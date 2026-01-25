package com.team.jubjub.data.repository

interface AuthRepository {

    /**
     * 1. [자동로그인] 현재 로그인된 유저 UID 확인
     * 앱 실행 시 가장 먼저 호출하여 로그인 여부 판단
     * @return String? : UID (null이면 비로그인 상태)
     */
    fun getCurrentUserUid(): String?

    /**
     * 2. [회원가입] 이메일과 비밀번호로 계정 생성
     * @return Result<String> : 성공 시 생성된 UID 반환
     */
    suspend fun signUp(
        email: String,
        password: String
    ): Result<String>

    /**
     * 3. [로그인] 이메일과 비밀번호로 로그인
     * @return Result<String> : 성공 시 로그인된 UID 반환
     */
    suspend fun signIn(
        email: String,
        password: String
    ): Result<String>

    /**
     * 4. [로그아웃] Firebase Auth 로그아웃
     */
    suspend fun signOut(): Result<Unit>

    /**
     * 5. [비밀번호 재설정] 비밀번호 재설정 이메일 전송
     * 로그인 실패 시 또는 보안 관리 차원에서 사용
     */
    suspend fun sendPasswordResetEmail(
        email: String
    ): Result<Unit>

    /**
     * 6. [회원탈퇴] Firebase Auth 계정 영구 삭제
     * 로그인 자격을 없애는 기능
     * 주의: Firestore 데이터 삭제는 UserRepository에서 선행되어야 함
     */
    suspend fun withdrawAccount(): Result<Unit>
}