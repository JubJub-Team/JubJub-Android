package com.team.jubjub

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JubJubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 필요 시 전역 초기화 로직 작성
    }
}