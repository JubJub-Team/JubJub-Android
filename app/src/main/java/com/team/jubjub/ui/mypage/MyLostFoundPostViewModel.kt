package com.team.jubjub.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.jubjub.data.model.Post
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
class MyLostFoundPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var origin: List<Post> = emptyList()

    fun loadMyLostPosts() {
        val uid = authRepository.getCurrentUserUid()
        if (uid.isNullOrBlank()) {
            _error.value = "로그인이 필요합니다."
            return
        }

        viewModelScope.launch {
            postRepository.getMyPostList(uid)
                .onSuccess { list ->
                    val filtered = list.filter { it.postType == PostType.LOST }
                    origin = filtered
                    _posts.value = filtered
                }
                .onFailure { e ->
                    _error.value = e.message ?: "내 분실/습득글을 불러오지 못했습니다."
                }
        }
    }

    fun search(keyword: String) {
        val k = keyword.trim()
        if (k.isBlank()) {
            _posts.value = origin
            return
        }
        _posts.value = origin.filter {
            it.title.contains(k, true) || it.content.contains(k, true)
        }
    }
}
