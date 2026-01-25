package com.team.jubjub.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.team.jubjub.data.model.enums.PostCategory
import com.team.jubjub.data.model.enums.PostStatus
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.model.enums.ProductCondition
import com.team.jubjub.data.model.enums.TradeMethod

// 게시글 정보 (posts 컬렉션 - 나눔/분실 통합)
data class Post(
    // 공통 필드
    @DocumentId // Firestore 문서 ID를 자동으로 매핑
    val postId: String = "", // 게시글 ID
    val postType: PostType = PostType.SHARING, // 게시글 유형 (나눔/분실)
    val school: String = "", // 학교명

    // 작성자 정보
    val writerUserId: String = "", // 작성자 User ID
    val writerCustomId: String = "", // 작성자 표시 ID
    val writerNickname: String = "", // 작성자 닉네임
    val writerProfileImageUrl: String = "", // 작성자 프로필 이미지 URL

    // 게시글 내용
    val title: String = "", // 제목
    val content: String = "", // 본문 내용
    val images: List<String> = emptyList(), // 이미지 URL 목록
    val status: PostStatus = PostStatus.AVAILABLE, // 게시글 상태 (판매중/예약중/완료)

    val keywords: List<String> = emptyList(), // 검색용 키워드 리스트

    @ServerTimestamp // 업로드 시 서버 시간 자동 기록
    val createdAt: Timestamp? = null,
    val commentCount: Int = 0, // 댓글 수

    // 나눔 전용 필드 (SHARING)
    val category: PostCategory? = null, // 물품 카테고리
    val productCondition: String? = null, // 물품 상태
    val quantity: Int? = 1, // 물품 수량
    val tradeMethods: List<TradeMethod>? = null, // 거래 방식 목록
    val hopeLocation: String? = null, // 희망 거래 장소

    // 분실 전용 필드 (LOST)
    val foundLocation: String? = null, // 습득 장소
    val foundDetailLocation: String? = null, // 습득 상세 장소
    val foundDate: Timestamp? = null, // 습득 일시 (Timestamp)
    val storageLocation: String? = null // 물품 보관 장소
)