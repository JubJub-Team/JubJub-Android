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

    private var _sourceList: List<Post> = emptyList()
    private val _postList = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> = _postList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentSchoolName: String? = null
    private var currentFilter: Int = FILTER_ALL

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
                _sourceList = it
                applyFilter()
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
                    _sourceList = list.filter { it.postType == PostType.SHARING }
                    applyFilter()
                }
                .onFailure {
                    _errorMessage.value = "검색 실패"
                }
        }
    }

    fun filterByStatus(statusIndex: Int) {
        currentFilter = statusIndex
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            FILTER_ALL -> _sourceList
            FILTER_AVAILABLE -> _sourceList.filter { it.status == PostStatus.AVAILABLE }
            FILTER_RESERVED -> _sourceList.filter { it.status == PostStatus.RESERVED }
            FILTER_COMPLETED -> _sourceList.filter { it.status == PostStatus.COMPLETED }
            FILTER_WITH_LOCATION -> _sourceList.filter { it.hasLocation }
            else -> _sourceList
        }
        _postList.value = filtered
    }

    private companion object {
        const val FILTER_ALL = 0
        const val FILTER_AVAILABLE = 1
        const val FILTER_RESERVED = 2
        const val FILTER_COMPLETED = 3
        const val FILTER_WITH_LOCATION = 4
    }
}
