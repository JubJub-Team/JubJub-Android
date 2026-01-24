package com.team.jubjub.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class TextTranslator {
    // 영어를 한국어로 번역할 때 옵션
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.KOREAN)
        .build()

    private val translator = Translation.getClient(options)

    // 번역 실행
    fun translate(text: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        // 와이파이가 아니어도 다운 가능하게 설정
        val conditions = DownloadConditions.Builder()
            .build()

        // 모델이 있는지 확인하고 없으면 다운로드
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                // 모델 준비 완료 후 번역 시작
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        onSuccess(translatedText)
                    }
                    .addOnFailureListener {
                        onFailure()
                    }
            }
            .addOnFailureListener {
                // 인터넷 연결 안 됨 등의 이유로 모델 다운 실패
                onFailure()
            }
    }

    fun close() {
        translator.close()
    }
}