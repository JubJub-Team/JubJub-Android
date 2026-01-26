package com.team.jubjub.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.jubjub.data.model.Notification
import com.team.jubjub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AlarmUiState {
    object Loading : AlarmUiState()
    data class Success(val items: List<AlarmUiItem>) : AlarmUiState()
    data class Error(val message: String) : AlarmUiState()
}

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AlarmUiState>(AlarmUiState.Loading)
    val state: StateFlow<AlarmUiState> = _state.asStateFlow()

    private var alarms: List<Alarm> = emptyList()

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = AlarmUiState.Loading

            userRepository.getNotificationList(userId)
                .onSuccess { list ->
                    alarms = list.map { it.toAlarm() }
                    _state.value = AlarmUiState.Success(buildAlarmUiItems(alarms))
                }
                .onFailure { e ->
                    _state.value = AlarmUiState.Error(e.message ?: "알림 불러오기 실패")
                }
        }
    }

    fun markRead(userId: String, alarm: Alarm) {
        if (alarm.isRead) return

        viewModelScope.launch {
            userRepository.readNotification(userId, alarm.id)
                .onSuccess {
                    alarms = alarms.map { a ->
                        if (a.id == alarm.id) a.copy(isRead = true) else a
                    }
                    _state.value = AlarmUiState.Success(buildAlarmUiItems(alarms))
                }
                .onFailure { e ->
                    _state.value = AlarmUiState.Error(e.message ?: "읽음 처리 실패")
                }
        }
    }
}

private fun Notification.toAlarm(): Alarm {
    return Alarm(
        id = notificationId,
        title = notificationMessage,
        createdAtMillis = createdAt?.time ?: 0L,
        isRead = isRead
    )
}
