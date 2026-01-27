package com.team.jubjub.ui.share

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
class ShareViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _postList = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> = _postList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadSharePosts(schoolName: String) {
        viewModelScope.launch {
            postRepository.getPostList(
                schoolName = schoolName,
                type = PostType.SHARING
            ).onSuccess {
                _postList.value = it
            }.onFailure { e ->
                e.printStackTrace()

                // ⭐ 실제 에러 메시지 표시
                _errorMessage.value =
                    e.message ?: e.toString()
            }
        }
    }

    fun searchPosts(schoolName: String, keyword: String) {
        viewModelScope.launch {
            postRepository.searchPosts(schoolName, keyword)
                .onSuccess {
                    _postList.value = it
                }
                .onFailure {
                    _errorMessage.value = "검색 실패"
                }
        }
    }
}
