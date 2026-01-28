package com.team.jubjub.ui.share

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostStatus
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.PostRepository
import com.team.jubjub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var _cachedList: List<Post> = emptyList()
    private val _postList = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> = _postList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentSchoolName: String? = null

    fun loadSharePosts() {
        viewModelScope.launch {
            if (currentSchoolName == null) {
                val uid = authRepository.getCurrentUserUid()
                if (uid != null) {
                    userRepository.getUserProfile(uid).onSuccess { user ->
                        currentSchoolName = user.school
                        fetchPostsInternal(user.school)
                    }
                }
            } else {
                fetchPostsInternal(currentSchoolName!!)
            }
        }
    }

    private suspend fun fetchPostsInternal(school: String) {
        postRepository.getPostList(school, PostType.SHARING)
            .onSuccess {
                _cachedList = it
                _postList.value = it
            }
            .onFailure {
                _errorMessage.value = it.message
            }
    }

    fun searchPosts(keyword: String) {
        val school = currentSchoolName ?: return
        viewModelScope.launch {
            postRepository.searchPosts(school, keyword)
                .onSuccess { list ->
                    _postList.value = list.filter { it.postType == PostType.SHARING }
                }
                .onFailure {
                    _errorMessage.value = "검색 실패"
                }
        }
    }

    fun filterByStatus(statusIndex: Int) {
        val currentSource = _cachedList
        val filtered = when (statusIndex) {
            0 -> currentSource
            1 -> currentSource.filter { it.status == PostStatus.AVAILABLE }
            2 -> currentSource.filter { it.status == PostStatus.RESERVED }
            3 -> currentSource.filter { it.status == PostStatus.COMPLETED }
            else -> currentSource
        }
        _postList.value = filtered
    }
}