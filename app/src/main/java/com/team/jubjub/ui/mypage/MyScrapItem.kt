package com.team.jubjub.ui.mypage

import com.team.jubjub.data.model.enums.PostType

data class MyScrapItem(
    val postId: String,
    val postType: PostType,
    val title: String,
    val preview: String,
    val locationTime: String, // "장소 · n분 전"
    val chatCount: Int
)
