package com.team.jubjub.data.model.enum

import androidx.annotation.DrawableRes
import com.team.jubjub.R

enum class UserLevel(
    val level: Int,
    @DrawableRes val levelBarRes: Int
) {
    LV1(1, R.drawable.ic_level_bar_1),
    LV2(2, R.drawable.ic_level_bar_2),
    LV3(3, R.drawable.ic_level_bar_3),
    LV4(4, R.drawable.ic_level_bar_4),
    LV5(5, R.drawable.ic_level_bar_5);

    companion object {
        fun from(level: Int): UserLevel {
            return values().firstOrNull { it.level == level } ?: LV1
        }
    }
}