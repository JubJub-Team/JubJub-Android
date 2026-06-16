package com.team.jubjub.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.team.jubjub.data.model.enums.PostCategory
import com.team.jubjub.data.model.enums.PostStatus
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.model.enums.ProductCondition
import com.team.jubjub.data.model.enums.TradeMethod
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
// 게시글 정보 (posts 컬렉션 - 나눔/분실 통합)
data class Post(
    @DocumentId
    val id: String = "",
    @get:PropertyName("postType")
    val postType: PostType = PostType.SHARING, // 게시글 유형 (나눔/분실)
    @get:PropertyName("school")
    val school: String = "", // 학교명

    // 작성자 정보
    @get:PropertyName("writerUserId")
    val writerUserId: String = "", // 작성자 User ID
    @get:PropertyName("writerCustomId")
    val writerCustomId: String = "", // 작성자 표시 ID
    @get:PropertyName("writerNickname")
    val writerNickname: String = "", // 작성자 닉네임
    @get:PropertyName("writerProfileImageUrl")
    val writerProfileImageUrl: String = "", // 작성자 프로필 이미지 URL

    // 게시글 내용
    @get:PropertyName("title")
    val title: String = "", // 제목
    @get:PropertyName("content")
    val content: String = "", // 본문 내용
    @get:PropertyName("images")
    val images: List<String> = emptyList(), // 이미지 URL 목록
    @get:PropertyName("status")
    val status: PostStatus = PostStatus.AVAILABLE, // 게시글 상태 (판매중/예약중/완료)

    @get:PropertyName("keywords")
    val keywords: List<String> = emptyList(), // 검색용 키워드 리스트

    @get:PropertyName("locationLatitude")
    val locationLatitude: Double? = null,
    @get:PropertyName("locationLongitude")
    val locationLongitude: Double? = null,

    @ServerTimestamp // 업로드 시 서버 시간 자동 기록
    @get:PropertyName("createdAt")
    val createdAt: Timestamp? = null,
    @get:PropertyName("commentCount")
    val commentCount: Int = 0, // 댓글 수

    // 나눔 전용 필드 (SHARING)
    @get:PropertyName("category")
    val category: PostCategory? = null, // 물품 카테고리
    @get:PropertyName("productCondition")
    val productCondition: ProductCondition? = null, // 물품 상태
    @get:PropertyName("quantity")
    val quantity: Int? = 1, // 물품 수량
    @get:PropertyName("tradeMethods")
    val tradeMethods: List<TradeMethod>? = null, // 거래 방식 목록
    @get:PropertyName("hopeLocation")
    val hopeLocation: String? = null, // 희망 거래 장소

    // 분실 전용 필드 (LOST)
    @get:PropertyName("foundLocation")
    val foundLocation: String? = null, // 습득 장소
    @get:PropertyName("foundDetailLocation")
    val foundDetailLocation: String? = null, // 습득 상세 장소
    @get:PropertyName("foundDate")
    val foundDate: Timestamp? = null, // 습득 일시 (Timestamp)
    @get:PropertyName("storageLocation")
    val storageLocation: String? = null // 물품 보관 장소
) {
    val postId: String get() = id
}
