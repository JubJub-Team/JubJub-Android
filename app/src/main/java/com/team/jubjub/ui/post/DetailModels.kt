package com.team.jubjub.ui.post

// 게시글 상세 화면의 헤더 데이터 모델
sealed class DetailHeader {
    data class Share(
        val idDate: String,
        val title: String,
        val category: String,
        val condition: String,
        val count: String,
        val content: String,
        val deliveryEnabled: Boolean,
        val directEnabled: Boolean,
        val location: String,
        val imageUrl: String?
    ) : DetailHeader()

    data class LostFound(
        val idDate: String,
        val title: String,
        val foundPlace: String,
        val detailPlace: String,
        val foundDate: String,
        val content: String,
        val entrustedPlace: String,
        val imageUrl: String?
    ) : DetailHeader()
}