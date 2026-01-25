package com.team.jubjub.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.team.jubjub.data.model.enums.PostType

import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.util.concurrent.atomic.AtomicInteger

class ImageAnalyzer(private val context: Context) {
    private val translator = TextTranslator()

    private var tflite: Interpreter? = null
    private var labels: List<String> = emptyList()

    // ML Kit 설정
    private val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.5f)
        .build()
    private val labeler = ImageLabeling.getClient(options)

    private val commonBlockList = setOf(
        "Pattern", "Design", "Text", "Font", "Line", "Angle", "Rectangle",
        "Square", "Flag", "Material", "Color", "Shape", "Art",
        "Insect", "Organism", "Animal", "Textile", "Fabric", "Product",
        "Hand", "Finger", "Thumb", "Nail", "Arm", "Skin", "Person", "Selfie",
        "Technology", "Electronic device", "Gadget"
    )

    private val similarGroups = listOf(
        setOf("airpods", "galaxybuds", "earbuds", "headphones"),
        setOf("mobile phone", "smartphone", "phone"),
        setOf("bag", "backpack", "luggage"),
        setOf("mouse", "computer mouse"),
        setOf("tumbler", "cup", "bottle"),
        setOf("wallet", "purse")
    )

    init {
        setupTeachableMachine()
    }

    private fun setupTeachableMachine() {
        try {
            val modelBuffer = FileUtil.loadMappedFile(context, "model.tflite")
            tflite = Interpreter(modelBuffer)

            try {
                val rawLabels = FileUtil.loadLabels(context, "labels.txt")
                labels = rawLabels.map { line ->
                    line.replace(Regex("^\\d+\\s+"), "").trim()
                }
                Log.d("JubJub_AI", "라벨 로드 완료: $labels")
            } catch (e: Exception) {
                Log.e("JubJub_AI", "labels.txt 로드 실패.")
            }

            Log.d("JubJub_AI", "TM 인터프리터 초기화 성공")
        } catch (e: Exception) {
            Log.e("JubJub_AI", "TM 초기화 오류: ${e.message}")
            e.printStackTrace()
        }
    }

    fun analyze(originalBitmap: Bitmap, type: PostType, onResult: (List<String>) -> Unit) {
        if (type == PostType.LOST) {
            analyzeWithTM(originalBitmap, onResult)
        } else {
            analyzeWithMLKit(originalBitmap, onResult)
        }
    }

    // Teachable Machine 로직
    private fun analyzeWithTM(bitmap: Bitmap, onResult: (List<String>) -> Unit) {
        val interpreter = tflite
        if (interpreter == null || labels.isEmpty()) {
            Log.e("JubJub_AI", "TM 사용 불가 상태로 ML Kit 실행")
            analyzeWithMLKit(bitmap, onResult)
            return
        }

        try {
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .add(NormalizeOp(0f, 255f))
                .build()

            var tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            val outputBuffer = Array(1) { FloatArray(labels.size) }
            interpreter.run(tensorImage.buffer, outputBuffer)

            val scores = outputBuffer[0]
            val maxScoreIndex = scores.indices.maxByOrNull { scores[it] } ?: -1

            if (maxScoreIndex != -1) {
                val topLabel = labels[maxScoreIndex]
                var topScore = scores[maxScoreIndex]

                Log.d("JubJub_AI", "TM 1위 예측: $topLabel ($topScore)")

                // 1등이 'Other'나 'None'이면 ML Kit로 넘김
                if (topLabel.equals("Other", ignoreCase = true) ||
                    topLabel.equals("None", ignoreCase = true)) {
                    Log.d("JubJub_AI", "TM 결과가 '$topLabel'이므로 ML Kit로 넘김")
                    analyzeWithMLKit(bitmap, onResult)
                    return
                }

                // 유사 그룹 점수 합산
                for (group in similarGroups) {
                    if (group.contains(topLabel.lowercase())) {
                        for (i in scores.indices) {
                            if (i == maxScoreIndex) continue
                            val otherLabel = labels[i].lowercase()
                            if (group.contains(otherLabel)) {
                                topScore += scores[i]
                            }
                        }
                        break
                    }
                }

                // 기준 점수 0.9
                if (topScore >= 0.90f) {
                    Log.d("JubJub_AI", "TM 최종 성공 ($topLabel, $topScore)")
                    processSingleKeyword(topLabel) { translated ->
                        onResult(listOf(translated))
                    }
                    return
                }
            }

        } catch (e: Exception) {
            Log.e("JubJub_AI", "TM 실행 중 오류: ${e.message}")
            e.printStackTrace()
        }

        Log.d("JubJub_AI", "TM 점수 미달로 ML Kit 실행")
        analyzeWithMLKit(bitmap, onResult)
    }

    private fun analyzeWithMLKit(bitmap: Bitmap, onResult: (List<String>) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        labeler.process(image)
            .addOnSuccessListener { resultLabels ->
                val validLabels = resultLabels.map { it.text }
                    .filter { !commonBlockList.contains(it) }
                    .distinct()
                    .take(5)

                if (validLabels.isEmpty()) {
                    onResult(listOf("기타"))
                    return@addOnSuccessListener
                }
                processKeywordList(validLabels, onResult)
            }
            .addOnFailureListener {
                onResult(listOf("분석 실패"))
            }
    }

    private fun processSingleKeyword(englishWord: String, onComplete: (String) -> Unit) {
        val mapped = LabelMapper.mapToKorean(englishWord)
        if (!mapped.isNullOrEmpty()) {
            onComplete(mapped[0])
        } else {
            translator.translate(englishWord,
                onSuccess = { onComplete(it) },
                onFailure = { onComplete(englishWord) }
            )
        }
    }

    private fun processKeywordList(englishList: List<String>, onResult: (List<String>) -> Unit) {
        val finalResult = mutableListOf<String>()
        val pendingTranslations = AtomicInteger(0)

        for (word in englishList) {
            val mapped = LabelMapper.mapToKorean(word)
            if (!mapped.isNullOrEmpty()) {
                finalResult.addAll(mapped)
            } else {
                pendingTranslations.incrementAndGet()
                translator.translate(word,
                    onSuccess = { translated ->
                        synchronized(finalResult) { finalResult.add(translated) }
                        checkIfFinished(pendingTranslations.decrementAndGet(), finalResult, onResult)
                    },
                    onFailure = {
                        checkIfFinished(pendingTranslations.decrementAndGet(), finalResult, onResult)
                    }
                )
            }
        }
        if (pendingTranslations.get() == 0) {
            checkIfFinished(0, finalResult, onResult)
        }
    }

    private fun checkIfFinished(pendingCount: Int, results: MutableList<String>, onResult: (List<String>) -> Unit) {
        if (pendingCount <= 0) {
            val distinctList = results.distinct().take(5)
            if (distinctList.isEmpty()) {
                onResult(listOf("기타"))
            } else {
                onResult(distinctList)
            }
        }
    }

    fun close() {
        tflite?.close()
        translator.close()
    }
}