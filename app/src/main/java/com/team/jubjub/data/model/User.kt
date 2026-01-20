package com.team.jubjub.data.model

import com.team.jubjub.data.model.enum.NotificationType
import com.team.jubjub.data.model.enum.PostStatus
import com.team.jubjub.data.model.enum.PostType
import java.util.Date

// 사용자 정보 (users 컬렉션)
data class User(
    val userId: String = "", // Firebase Auth UID
    val customId: String = "", // 사용자 지정 ID
    val name: String = "", // 실명
    val nickname: String = "", // 닉네임
    val school: String = "", // 학교명
    val email: String = "", // 이메일
    val phone: String = "", // 전화번호
    val birthDate: String = "", // 생년월일(YYYYMMDD)
    val profileImageUrl: String = "", // 프로필 이미지 URL
    val sharingCount: Int = 0, // 나눔 완료 횟수
    val fcmToken: String = "", // 푸시 알림 토큰

    val createdAt: Date? = null, // 계정 생성 일시
    val updatedAt: Date? = null // 마지먹 정보 수정 일시
)

// 스크랩 목록 (users/{userId}/scraps 하위 컬렉션)
data class Scrap(
    val postId: String = "", // 게시글 ID
    val postType: PostType = PostType.SHARING, // 게시글 유형
    val title: String = "", // 게시글 제목
    val postThumbnail: String = "", // 게시글 대표 이미지 URL
    val status: PostStatus = PostStatus.AVAILABLE, // 게시글 상태
    val scrappedAt: Date? = null // 스크랩 일시
)

// 알림 목록 (users/{userId}/notifications 하위 컬렉션)
data class Notification(
    val notificationId: String = "", // 알림 ID
    val notificationType: NotificationType = NotificationType.COMMENT, // 알림 유형
    val notificationMessage: String = "", // 알림 메시지 내용
    val targetPostId: String = "", // 이동할 게시글 ID
    val isRead: Boolean = false, // 읽음 여부 (Default: False)
    val createdAt: Date? = null // 알림 생성 일시
)