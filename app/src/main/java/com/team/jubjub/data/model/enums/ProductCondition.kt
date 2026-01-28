package com.team.jubjub.data.model.enums

import com.google.firebase.firestore.PropertyName

enum class ProductCondition {
    @PropertyName("New")
    NEW,        // 새상품

    @PropertyName("LikeNew")
    LIKE_NEW,   // 사용감 없음

    @PropertyName("LightUse")
    LIGHT_USE,  // 사용감 적음

    @PropertyName("HeavyUse")
    HEAVY_USE,  // 사용감 많음

    @PropertyName("Damaged")
    DAMAGED     // 고장 및 파손
}