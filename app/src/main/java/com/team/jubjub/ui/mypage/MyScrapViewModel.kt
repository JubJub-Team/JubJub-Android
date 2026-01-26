package com.team.jubjub.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyScrapViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<MyScrapItem>>(emptyList())
    val items: StateFlow<List<MyScrapItem>> = _items.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadScraps() {
        val uid = authRepository.getCurrentUserUid()
        if (uid.isNullOrBlank()) {
            _error.value = "로그인이 필요합니다."
            return
        }

        viewModelScope.launch {
            postRepository.getScrappedPostList(uid)
                .onSuccess { posts ->
                    val mapped = posts
                        .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L } // 최신순
                        .map { post ->
                            val location = when (post.postType) {
                                PostType.SHARING -> post.hopeLocation ?: "장소 미정"
                                PostType.LOST -> post.foundLocation ?: post.storageLocation ?: "장소 미정"
                            }

                            val timeAgo = formatTimeAgo(post.createdAt)

                            MyScrapItem(
                                postId = post.postId,
                                postType = post.postType,
                                title = post.title,
                                preview = post.content,
                                locationTime = "$location · $timeAgo",
                                chatCount = post.commentCount
                            )
                        }

                    _items.value = mapped
                }
                .onFailure { e ->
                    _error.value = e.message ?: "관심 나눔 목록을 불러오지 못했습니다."
                }
        }
    }

    private fun formatTimeAgo(createdAt: Timestamp?): String {
        if (createdAt == null) return "방금 전"

        val now = System.currentTimeMillis()
        val time = createdAt.toDate().time
        val diff = now - time

        val minute = 60_000L
        val hour = 60 * minute
        val day = 24 * hour

        return when {
            diff < minute -> "방금 전"
            diff < hour -> "${diff / minute}분 전"
            diff < day -> "${diff / hour}시간 전"
            else -> "${diff / day}일 전"
        }
    }
}
