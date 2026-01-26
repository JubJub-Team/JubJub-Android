package com.team.jubjub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.team.jubjub.data.model.Notification
import com.team.jubjub.data.model.Scrap
import com.team.jubjub.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore // Hilt가 넣어줌 (직접 getInstance 호출 X)
) : UserRepository {

    private val userRef = db.collection("users")

    // 1-1. 아이디 중복 확인
    override suspend fun checkCustomIdDuplicate(customId: String): Result<Boolean> {
        return checkDuplicate("customId", customId)
    }

    // 1-2. 닉네임 중복 확인
    override suspend fun checkNicknameDuplicate(nickname: String): Result<Boolean> {
        return checkDuplicate("nickname", nickname)
    }

    // 1-3. 이메일 중복 확인
    override suspend fun checkEmailDuplicate(email: String): Result<Boolean> {
        return checkDuplicate("email", email)
    }

    // 1-4. 전화번호 중복 확인
    override suspend fun checkPhoneDuplicate(phone: String): Result<Boolean> {
        return checkDuplicate("phone", phone)
    }

    // [내부 함수] 중복 체크 헬퍼 (쿼리 결과가 비어있으면 true = 중복 아님/사용 가능)
    private suspend fun checkDuplicate(field: String, value: String): Result<Boolean> {
        return try {
            val snapshot = userRef.whereEqualTo(field, value).limit(1).get().await()
            Result.success(snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. 정보 저장/수정
    override suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            // userId를 문서 ID로 사용 (set)
            userRef.document(user.userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. 내 정보 가져오기
    override suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val document = userRef.document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. 스크랩 목록
    override suspend fun getScrapList(userId: String): Result<List<Scrap>> {
        return try {
            val snapshot = userRef.document(userId)
                .collection("scraps")
                .orderBy("scrappedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val list = snapshot.toObjects(Scrap::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. 알림 목록
    override suspend fun getNotificationList(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = userRef.document(userId)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val list = snapshot.toObjects(Notification::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. 알림 읽음 처리
    override suspend fun readNotification(userId: String, notificationId: String): Result<Unit> {
        return try {
            userRef.document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 7. 회원 탈퇴 (DB 데이터 삭제)
    override suspend fun deleteAllUserData(userId: String): Result<Unit> {
        return try {
            userRef.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}