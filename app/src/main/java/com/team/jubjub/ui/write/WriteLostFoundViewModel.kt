package com.team.jubjub.ui.write

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostStatus
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.ImageUploadRepository
import com.team.jubjub.data.repository.PostRepository
import com.team.jubjub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class WriteLostFoundViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val imageUploadRepository: ImageUploadRepository
) : ViewModel() {

    sealed class Event {
        object UploadSuccess : Event()
        data class Validation(val message: String) : Event()
        data class Fail(val message: String) : Event()
    }

    private val _event = Channel<Event>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()
    private val _userSchool = MutableStateFlow<String?>(null)
    val userSchool = _userSchool.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserUid()

            // 로그인이 안 된 상태
            if (uid.isNullOrBlank()) {
                // 로그를 남기고 종료
                android.util.Log.e("WriteViewModel", "로그인되지 않은 사용자입니다.")
                return@launch
            }

            userRepository.getUserProfile(uid)
                .onSuccess { user ->
                    // 학교 정보가 비어있으면 null로 두어 기본값(서울시청)을 쓰게 유도
                    _userSchool.value = user.school.ifBlank { null }
                }
                .onFailure { e ->
                    // 로드 실패 시 처리 (로그 출력)
                    e.printStackTrace()
                    android.util.Log.e("WriteViewModel", "유저 정보 로드 실패: ${e.message}")
                }
        }
    }

    fun uploadLostPostWithImage(
        title: String,
        content: String,
        foundLocation: String?,
        foundDetailLocation: String?,
        foundDate: Timestamp?,
        storageLocation: String?,
        locationLatitude: Double?,
        locationLongitude: Double?,
        imageUri: Uri?,
        keywords: List<String> = emptyList() // Fragment에서 합친 최종 키워드
    ) {
        val t = title.trim()
        val c = content.trim()

        if (t.isBlank()) {
            viewModelScope.launch { _event.send(Event.Validation("제목을 입력해주세요.")) }
            return
        }
        if (c.isBlank()) {
            viewModelScope.launch { _event.send(Event.Validation("내용을 입력해주세요.")) }
            return
        }

        val uid = authRepository.getCurrentUserUid()
        if (uid.isNullOrBlank()) {
            viewModelScope.launch { _event.send(Event.Fail("로그인이 필요해요.")) }
            return
        }

        viewModelScope.launch {
            val user = userRepository.getUserProfile(uid)
                .getOrElse {
                    _event.send(Event.Fail("유저 정보 조회 실패: ${it.message}"))
                    return@launch
                }

            if (user.school.isBlank()) {
                _event.send(Event.Fail("유저 학교 정보가 없어요."))
                return@launch
            }

            // 1) 이미지 업로드(선택한 경우만)
            val imageUrls: List<String> =
                if (imageUri != null) {
                    val url = imageUploadRepository
                        .uploadImage(imageUri, folder = "posts/$uid")
                        .getOrElse {
                            _event.send(Event.Fail("이미지 업로드 실패: ${it.message}"))
                            return@launch
                        }
                    listOf(url)
                } else {
                    emptyList()
                }

            val post = Post(
                postType = PostType.LOST,
                school = user.school,

                writerUserId = uid,
                writerCustomId = user.customId,
                writerNickname = user.nickname,
                writerProfileImageUrl = user.profileImageUrl,

                title = t,
                content = c,
                images = imageUrls,
                status = PostStatus.AVAILABLE,

                // Fragment에서 해시태그 + AI태그 합쳐서 넘겨준 최종 키워드 사용
                keywords = keywords,
                locationLatitude = locationLatitude,
                locationLongitude = locationLongitude,

                foundLocation = foundLocation?.trim().takeIf { !it.isNullOrBlank() },
                foundDetailLocation = foundDetailLocation?.trim().takeIf { !it.isNullOrBlank() },
                foundDate = foundDate,
                storageLocation = storageLocation?.trim().takeIf { !it.isNullOrBlank() }
            )

            postRepository.uploadPost(post)
                .onSuccess { _event.send(Event.UploadSuccess) }
                .onFailure { _event.send(Event.Fail("업로드 실패: ${it.message}")) }
        }
    }
}
