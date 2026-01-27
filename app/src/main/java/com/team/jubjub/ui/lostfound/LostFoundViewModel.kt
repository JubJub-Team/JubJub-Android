package com.team.jubjub.ui.lostfound

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LostFoundViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _postList = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> = _postList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadLostPosts(schoolName: String) {
        viewModelScope.launch {
            postRepository.getPostList(
                schoolName = schoolName,
                type = PostType.LOST
            ).onSuccess {
                _postList.value = it
            }.onFailure { e ->
                e.printStackTrace()
                _errorMessage.value = e.message ?: e.toString()
            }
        }
    }

    fun searchPosts(schoolName: String, keyword: String) {
        viewModelScope.launch {
            postRepository.searchPosts(schoolName, keyword)
                .onSuccess { list ->
                    // 검색 결과 중 LOST만 남김 (searchPosts는 타입필터가 없음)
                    _postList.value = list.filter { it.postType == PostType.LOST }
                }
                .onFailure { e ->
                    e.printStackTrace()
                    _errorMessage.value = e.message ?: "검색 실패"
                }
        }
    }
}
