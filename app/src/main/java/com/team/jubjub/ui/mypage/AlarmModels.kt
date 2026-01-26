package com.team.jubjub.ui.mypage

import java.util.concurrent.TimeUnit

data class Alarm(
    val id: String,
    val title: String,
    val createdAtMillis: Long,
    val isRead: Boolean
)

sealed class AlarmUiItem {
    data class Header(val title: String) : AlarmUiItem()
    data class Row(val alarm: Alarm) : AlarmUiItem()
}

fun buildAlarmUiItems(alarms: List<Alarm>): List<AlarmUiItem> {
    if (alarms.isEmpty()) return emptyList()

    val now = System.currentTimeMillis()
    val sorted = alarms.sortedByDescending { it.createdAtMillis }

    fun daysAgo(time: Long): Long =
        TimeUnit.MILLISECONDS.toDays(now - time).coerceAtLeast(0)

    val today = mutableListOf<Alarm>()
    val yesterday = mutableListOf<Alarm>()
    val last7days = mutableListOf<Alarm>()
    val past = mutableListOf<Alarm>()

    for (a in sorted) {
        when (daysAgo(a.createdAtMillis)) {
            0L -> today.add(a)
            1L -> yesterday.add(a)
            in 2L..6L -> last7days.add(a)
            else -> past.add(a)
        }
    }

    val result = mutableListOf<AlarmUiItem>()

    fun addSection(header: String, list: List<Alarm>) {
        if (list.isEmpty()) return
        result.add(AlarmUiItem.Header(header))
        result.addAll(list.map { AlarmUiItem.Row(it) })
    }

    addSection("오늘", today)
    addSection("어제", yesterday)
    addSection("지난 7일", last7days)
    addSection("지난 활동", past)

    return result
}
