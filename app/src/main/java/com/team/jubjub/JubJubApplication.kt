package com.team.jubjub

import android.app.Application
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JubJubApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 네이버 지도 SDK 초기화
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NcpKeyClient(BuildConfig.NAVER_CLIENT_ID)
    }
}