package com.team.jubjub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth // Hilt가 주입해줌
) : AuthRepository {

    // 1. 현재 유저 확인
    override fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    // 2. 회원가입
    override suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: throw Exception("UID 생성 실패")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. 로그인
    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid
                ?: throw Exception("로그인 실패: 유저 정보를 가져올 수 없습니다.")
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. 로그아웃
    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. 비밀번호 재설정 이메일 보내기
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. 회원 탈퇴 (Auth 계정 삭제)
    override suspend fun withdrawAccount(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: throw Exception("로그인된 사용자가 없습니다.")

            // Firebase Auth 계정 삭제
            currentUser.delete().await()
            Result.success(Unit)
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            // 참고: 로그인한 지 오래된 경우 재인증(re-authentication)이 필요할 수 있음
            Result.failure(Exception("REAUTHENTICATION_REQUIRED"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}