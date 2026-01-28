package com.team.jubjub.ui.lostfound

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
class LostFoundViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var _cachedList: List<Post> = emptyList()
    private val _postList = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> = _postList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // 학교 이름을 저장해둘 변수
    private var currentSchoolName: String? = null

    fun loadLostPosts() {
        viewModelScope.launch {
            // 학교 이름이 아직 없으면 가져오기
            if (currentSchoolName == null) {
                val uid = authRepository.getCurrentUserUid()
                if (uid != null) {
                    userRepository.getUserProfile(uid).onSuccess { user ->
                        currentSchoolName = user.school
                        fetchPostsInternal(user.school) // 게시글 가져오기
                    }
                }
            } else {
                // 이미 학교 이름을 알면 바로 게시글 가져오기
                fetchPostsInternal(currentSchoolName!!)
            }
        }
    }

    // 내부적으로 실제로 게시글을 긁어오는 함수
    private suspend fun fetchPostsInternal(school: String) {
        postRepository.getPostList(schoolName = school, type = PostType.LOST)
            .onSuccess {
                _cachedList = it
                _postList.value = it
            }
            .onFailure {
                _errorMessage.value = it.message
            }
    }

    // 검색 함수
    fun searchPosts(keyword: String) {
        val school = currentSchoolName ?: return // 학교 정보 없으면 검색 불가

        viewModelScope.launch {
            postRepository.searchPosts(school, keyword)
                .onSuccess { list ->
                    _postList.value = list.filter { it.postType == PostType.LOST }
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
            2 -> currentSource.filter { it.status == PostStatus.COMPLETED }
            else -> currentSource
        }
        _postList.value = filtered
    }
}