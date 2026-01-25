package com.team.jubjub.data.model

import java.util.Date

// 댓글 정보 (posts/{postId}/comments 하위 컬렉션)
data class Comment(
    val commentId: String = "", // 댓글 ID

    // 작성자 정보
    val writerUserId: String = "", // 작성자 User ID
    val writerNickname: String = "", // 작성자 닉네임
    val writerProfileImageUrl: String = "", // 작성자 프로필 이미지 URL

    val content: String = "", // 댓글 내용
    val isSecret: Boolean = false, // 비밀 댓글 여부

    val parentId: String? = null, // 부모 댓글 ID (대댓글인 경우)
    val createdAt: Date? = null // 댓글 작성 일시
)