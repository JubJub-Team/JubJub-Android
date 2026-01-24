package com.team.jubjub.ui.post

data class PostHeader(
    val idDate: String,
    val title: String,
    val category: String,
    val condition: String,
    val count: String,
    val content: String,
    val deliveryEnabled: Boolean,
    val directEnabled: Boolean,
    val location: String
)

data class Comment(
    val nickname: String,
    val timeText: String,
    val body: String,
    val isReply: Boolean = false
)
