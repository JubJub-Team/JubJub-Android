package com.team.jubjub.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 앱 전역에서 사용되는 외부 라이브러리 및 Repository 의존성 주입 모듈
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 1. Firebase Authentication 인스턴스 제공
     *
     * @return FirebaseAuth : 사용자 인증 관리 객체
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * 2. Firebase Firestore 인스턴스 제공
     *
     * @return FirebaseFirestore : NoSQL 데이터베이스 관리 객체
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    /**
     * 3. Firebase Storage 인스턴스 제공
     * (추후 이미지 업로드 기능 구현 시 사용)
     *
     * @return FirebaseStorage : 파일 저장소 관리 객체
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}