package com.team.jubjub.util

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleCoroutineScope
import com.team.jubjub.R
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.UserRepository
import kotlinx.coroutines.launch

fun Toolbar.updateAlarmBadgeIcon(
    lifecycleScope: LifecycleCoroutineScope,
    authRepository: AuthRepository,
    userRepository: UserRepository
) {
    val alarmItem = menu.findItem(R.id.action_alarm) ?: return

    val userId = authRepository.getCurrentUserUid()
    if (userId.isNullOrBlank()) {
        alarmItem.setIcon(R.drawable.notification)
        return
    }

    lifecycleScope.launch {
        userRepository.getNotificationList(userId)
            .onSuccess { list ->
                val hasUnread = list.any { !it.isRead }
                alarmItem.setIcon(
                    if (hasUnread) R.drawable.ic_alarm_red_dot else R.drawable.notification
                )
            }
            .onFailure {
                alarmItem.setIcon(R.drawable.notification)
            }
    }
}
