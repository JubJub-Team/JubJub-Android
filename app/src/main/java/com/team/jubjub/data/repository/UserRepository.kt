package com.team.jubjub.data.repository

import android.net.Uri
import com.team.jubjub.data.model.Notification
import com.team.jubjub.data.model.Scrap
import com.team.jubjub.data.model.User

interface UserRepository {

    // --- 1. 중복 확인 로직 ---
    /**
     * 1-1. [중복체크] 커스텀 아이디 중복 확인
     * @param customId : 사용자가 입력한 ID (예: Abc1234)
     * @return Result<Boolean> : true=사용가능, false=중복
     */
    suspend fun checkCustomIdDuplicate(
        customId: String
    ): Result<Boolean>

    /**
     * 1-2. [중복체크] 닉네임 중복 확인
     * @param nickname : 사용자가 입력한 닉네임
     */
    suspend fun checkNicknameDuplicate(
        nickname: String
    ): Result<Boolean>

    /**
     * 1-3. [중복체크] 이메일 중복 확인
     * Auth 레벨 외에 DB 상에서의 중복 여부 확인
     */
    suspend fun checkEmailDuplicate(
        email: String
    ): Result<Boolean>

    /**
     * 1-4. [중복체크] 전화번호 중복 확인
     */
    suspend fun checkPhoneDuplicate(
        phone: String
    ): Result<Boolean>

    /**
     * 2. [프로필] 유저 정보 저장 및 수정
     * 회원가입 완료 시 또는 프로필 수정 시 호출
     * @param user : 저장할 사용자 객체
     */
    suspend fun saveUserProfile(
        user: User
    ): Result<Unit>

    /**
     * 3. [프로필] 내 정보 가져오기
     * 마이페이지 등 진입 시 호출
     * @param userId : Target User UID
     */
    suspend fun getUserProfile(
        userId: String
    ): Result<User>

    /**
     * 4. [스크랩] 스크랩 목록 조회
     * 마이페이지 > 관심 나눔
     */
    suspend fun getScrapList(
        userId: String
    ): Result<List<Scrap>>

    /**
     * 5. [알림] 알림 목록 조회
     * 마이페이지 > 알림
     */
    suspend fun getNotificationList(
        userId: String
    ): Result<List<Notification>>

    /**
     * 6. [알림] 알림 읽음 처리
     * 알림 클릭 시 호출하여 '읽음' 상태로 업데이트
     */
    suspend fun readNotification(
        userId: String,
        notificationId: String
    ): Result<Unit>

    /**
     * 7. [회원탈퇴] 유저 데이터 삭제
     * Firestore 내 해당 유저의 문서를 삭제
     * (Auth 계정 삭제 전 실행되어야 함)
     */
    suspend fun deleteAllUserData(
        userId: String
    ): Result<Unit>

    /**
     * 8. [알림] 알림 생성 및 전송
     * 댓글 작성, 키워드 일치 등 이벤트 발생 시 상대방의 알림함에 추가
     * @param targetUserId : 알림을 받을 사용자의 UID
     * @param notification : 생성할 알림 객체
     */
    suspend fun sendNotification(
        targetUserId: String,
        notification: Notification
    ): Result<Unit>

    /**
     * 9. [이미지] 프로필 이미지 업로드
     * Storage에 이미지를 업로드하고 다운로드 URL을 반환
     * @param userId : 파일 경로 생성을 위한 User UID
     * @param uri : 갤러리에서 선택된 이미지 URI
     * @return Result<String> : 업로드된 이미지의 URL
     */
    suspend fun uploadProfileImage(
        userId: String,
        uri: Uri
    ): Result<String>
}