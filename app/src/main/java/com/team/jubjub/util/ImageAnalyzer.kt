package com.team.jubjub.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.team.jubjub.data.model.enum.PostType

import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.support.label.Category

class ImageAnalyzer(private val context: Context) {
    private val translator = TextTranslator()
    private var classifier: ImageClassifier? = null
    private val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.6f)
        .build()
    private val labeler = ImageLabeling.getClient(options)

    // 차단 목록
    private val commonBlockList = setOf(
        "Pattern", "Design", "Text", "Font", "Line", "Angle", "Rectangle",
        "Square", "Flag", "Material", "Color", "Shape", "Art",
        "Insect", "Organism", "Animal", "Textile", "Fabric", "Product",
        "Hand", "Finger", "Thumb", "Nail", "Arm", "Skin", "Person", "Selfie",
        "Technology", "Electronic device", "Gadget" // 너무 포괄적인 단어도 차단
    )

    // 유사 그룹 정의 (TM용)
    private val similarGroups = listOf(
        setOf("airpods", "galaxybuds"),
        setOf("mobile phone", "smartphone"),
        setOf("bag", "backpack")
    )

    init {
        setupTeachableMachine()
    }

    private fun setupTeachableMachine() {
        try {
            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setMaxResults(3)
                .build()
            classifier = ImageClassifier.createFromFileAndOptions(
                context, "model.tflite", options
            )
        } catch (e: Exception) {
            Log.e("JubJub_AI", "Teachable Machine 초기화 실패: ${e.message}")
        }
    }

    fun analyze(originalBitmap: Bitmap, type: PostType, onResult: (List<String>) -> Unit) {

        // 게시판 타입에 따라 분기 처리
        if (type == PostType.LOST) {
            // 분실물: TM -> 실패시 ML Kit
            analyzeWithTM(originalBitmap, type, onResult)
        } else {
            // 나눔: 바로 ML Kit
            analyzeWithMLKit(originalBitmap, type, onResult)
        }
    }

    // Teachable Machine 로직
    private fun analyzeWithTM(bitmap: Bitmap, type: PostType, onResult: (List<String>) -> Unit) {
        val currentClassifier = classifier

        if (currentClassifier == null) {
            analyzeWithMLKit(bitmap, type, onResult)
            return
        }

        try {
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val classifications: List<Classifications> = currentClassifier.classify(tensorImage)

            if (classifications.isNotEmpty() && classifications[0].categories.isNotEmpty()) {
                val results: List<Category> = classifications[0].categories
                val topResult = results[0]
                var totalConfidence = topResult.score
                val topLabel = topResult.label.lowercase()

                // 유사 그룹 점수 합산
                for (group in similarGroups) {
                    if (group.contains(topLabel)) {
                        for (i in 1 until results.size) {
                            val otherLabel = results[i].label.lowercase()
                            if (group.contains(otherLabel)) {
                                totalConfidence += results[i].score
                            }
                        }
                        break
                    }
                }

                // 85% 이상이면 채택
                if (totalConfidence >= 0.85f) {
                    Log.d("JubJub_AI", "TM 성공 ($topLabel, $totalConfidence)")

                    // 매퍼에 없으면 번역기 돌리기
                    processSingleKeyword(topResult.label) { translatedKeyword ->
                        onResult(listOf(translatedKeyword))
                    }
                    return
                }
            }
        } catch (e: Exception) {
            Log.e("JubJub_AI", "TM 오류: ${e.message}")
        }

        // TM 실패 시 ML Kit로 넘어감
        Log.d("JubJub_AI", "TM 실패 또는 점수 미달 -> ML Kit 실행")
        analyzeWithMLKit(bitmap, type, onResult)
    }

    // ML Kit 로직
    private fun analyzeWithMLKit(bitmap: Bitmap, type: PostType, onResult: (List<String>) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                // 차단 목록 필터링
                val validLabels = labels.map { it.text }
                    .filter { !commonBlockList.contains(it) }
                    .distinct()
                    .take(5) // 최대 5개까지만 처리

                if (validLabels.isEmpty()) {
                    onResult(listOf("기타"))
                    return@addOnSuccessListener
                }

                // 매퍼 확인 + 번역 (비동기 처리)
                processKeywordList(validLabels, onResult)
            }
            .addOnFailureListener {
                onResult(listOf("분석 실패"))
            }
    }

    // 단일 키워드 처리 (TM용)
    private fun processSingleKeyword(englishWord: String, onComplete: (String) -> Unit) {
        // 매퍼에 있는지 확인
        val mapped = LabelMapper.mapToKorean(englishWord)
        if (!mapped.isNullOrEmpty()) {
            // 매퍼에 있으면 첫 번째 대표 단어 반환
            onComplete(mapped[0])
        } else {
            // 매퍼에 없으면 번역기 실행
            translator.translate(englishWord,
                onSuccess = { translated -> onComplete(translated) },
                onFailure = { onComplete(englishWord) } // 번역 실패 시 그냥 영어 반환
            )
        }
    }

    // 리스트 키워드 처리 (ML Kit용)
    private fun processKeywordList(englishList: List<String>, onResult: (List<String>) -> Unit) {
        val finalResult = mutableListOf<String>()
        var pendingTranslations = 0 // 번역 대기 중인 개수

        for (word in englishList) {
            // 매퍼 우선 검색
            val mapped = LabelMapper.mapToKorean(word)

            if (!mapped.isNullOrEmpty()) {
                // 매퍼에 있으면 바로 추가
                finalResult.addAll(mapped)
            } else {
                // 없으면 번역해야 함 -> 대기 카운트 증가
                pendingTranslations++
                translator.translate(word,
                    onSuccess = { translated ->
                        synchronized(finalResult) { finalResult.add(translated) }
                        pendingTranslations--
                        checkIfFinished(pendingTranslations, finalResult, onResult)
                    },
                    onFailure = {
                        pendingTranslations--
                        checkIfFinished(pendingTranslations, finalResult, onResult)
                    }
                )
            }
        }

        // 번역할 게 하나도 없었다면 바로 결과 반환
        if (pendingTranslations == 0) {
            checkIfFinished(0, finalResult, onResult)
        }
    }

    // 모든 작업이 끝났는지 확인하고 반환
    private fun checkIfFinished(pending: Int, results: MutableList<String>, onResult: (List<String>) -> Unit) {
        if (pending <= 0) {
            val distinctList = results.distinct().take(5)
            if (distinctList.isEmpty()) {
                onResult(listOf("기타"))
            } else {
                onResult(distinctList)
            }
        }
    }

    fun close() {
        classifier?.close()
        translator.close()
    }
}