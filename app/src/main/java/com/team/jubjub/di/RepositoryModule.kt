package com.team.jubjub.di

import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.AuthRepositoryImpl
import com.team.jubjub.data.repository.PostRepository
import com.team.jubjub.data.repository.PostRepositoryImpl
import com.team.jubjub.data.repository.UserRepository
import com.team.jubjub.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.team.jubjub.data.repository.ImageUploadRepository
import com.team.jubjub.data.repository.ImageUploadRepositoryImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * 1. AuthRepository 의존성 주입
     * 로그인, 회원가입 등 인증 관련 로직 구현체 반환
     *
     * @return AuthRepository : AuthRepositoryImpl 객체
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * 2. UserRepository 의존성 주입
     * 사용자 프로필, 닉네임 관리 관련 로직 구현체 반환
     *
     * @return UserRepository : UserRepositoryImpl 객체
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    /**
     * 3. PostRepository 의존성 주입
     * 게시물 CRUD, 검색, 스크랩 관련 로직 구현체 반환
     *
     * @return PostRepository : PostRepositoryImpl 객체
     */
    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    /**
     * 4. ImageUploadRepository 의존성 주입
     * Firebase Storage에 이미지 업로드 후 다운로드 URL 반환
     */
    @Binds
    @Singleton
    abstract fun bindImageUploadRepository(
        impl: ImageUploadRepositoryImpl
    ): ImageUploadRepository


}