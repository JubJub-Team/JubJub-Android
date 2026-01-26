package com.team.jubjub.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.jubjub.data.model.User
import com.team.jubjub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _idAvailable = MutableStateFlow<Boolean?>(null)
    val idAvailable: StateFlow<Boolean?> = _idAvailable

    private val _nicknameAvailable = MutableStateFlow<Boolean?>(null)
    val nicknameAvailable: StateFlow<Boolean?> = _nicknameAvailable

    private val _emailAvailable = MutableStateFlow<Boolean?>(null)
    val emailAvailable: StateFlow<Boolean?> = _emailAvailable

    private val _phoneAvailable = MutableStateFlow<Boolean?>(null)
    val phoneAvailable: StateFlow<Boolean?> = _phoneAvailable

    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message

    fun loadProfile(userId: String) = viewModelScope.launch {
        userRepository.getUserProfile(userId)
            .onSuccess { _user.value = it }
            .onFailure { _message.emit("프로필 불러오기 실패: ${it.message}") }
    }

    fun checkCustomId(customId: String) = viewModelScope.launch {
        userRepository.checkCustomIdDuplicate(customId)
            .onSuccess { _idAvailable.value = it }
            .onFailure { _idAvailable.value = null; _message.emit("아이디 중복확인 실패: ${it.message}") }
    }

    fun checkNickname(nickname: String) = viewModelScope.launch {
        userRepository.checkNicknameDuplicate(nickname)
            .onSuccess { _nicknameAvailable.value = it }
            .onFailure { _nicknameAvailable.value = null; _message.emit("닉네임 중복확인 실패: ${it.message}") }
    }

    fun checkEmail(email: String) = viewModelScope.launch {
        userRepository.checkEmailDuplicate(email)
            .onSuccess { _emailAvailable.value = it }
            .onFailure { _emailAvailable.value = null; _message.emit("이메일 중복확인 실패: ${it.message}") }
    }

    fun checkPhone(phone: String) = viewModelScope.launch {
        userRepository.checkPhoneDuplicate(phone)
            .onSuccess { _phoneAvailable.value = it }
            .onFailure { _phoneAvailable.value = null; _message.emit("전화번호 중복확인 실패: ${it.message}") }
    }

    fun saveProfile(updated: User) = viewModelScope.launch {
        val prev = _user.value
        val now = Date()

        // createdAt 유지, updatedAt 갱신
        val toSave = updated.copy(
            createdAt = prev?.createdAt ?: now,
            updatedAt = now,
            sharingCount = prev?.sharingCount ?: updated.sharingCount,
            fcmToken = prev?.fcmToken ?: updated.fcmToken,
            profileImageUrl = prev?.profileImageUrl ?: updated.profileImageUrl
        )

        userRepository.saveUserProfile(toSave)
            .onSuccess { _message.emit("저장 완료!") }
            .onFailure { _message.emit("저장 실패: ${it.message}") }
    }
}
