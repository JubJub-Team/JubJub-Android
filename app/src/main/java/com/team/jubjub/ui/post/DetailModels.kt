package com.team.jubjub.ui.post

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
        val location: String
    ) : DetailHeader()

    data class LostFound(
        val idDate: String,
        val title: String,
        val foundPlace: String,
        val detailPlace: String,
        val foundDate: String,
        val content: String,
        val entrustedPlace: String
    ) : DetailHeader()
}

data class Comment(
    val nickname: String,
    val timeText: String,
    val body: String,
    val isReply: Boolean = false
)
