package com.team.jubjub.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.PostRepository
import com.team.jubjub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val sharingTop2: List<Post> = emptyList(),
    val lostTop2: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // [홈] 화면 데이터 로드 (나눔/분실 최신글 각 2개)
    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val uid = authRepository.getCurrentUserUid()
            if (uid.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "로그인이 필요합니다.") }
                return@launch
            }

            try {
                // 1) 유저 프로필에서 학교명 가져오기
                val user = userRepository.getUserProfile(uid).getOrThrow()
                val schoolName = user.school

                // 2) 게시물 가져오기 후 최신순 2개만 자르기 (프론트 컷)
                val sharingTop2 = postRepository.getPostList(schoolName, PostType.SHARING)
                    .getOrElse { emptyList() }
                    .take(2)

                val lostTop2 = postRepository.getPostList(schoolName, PostType.LOST)
                    .getOrElse { emptyList() }
                    .take(2)

                _uiState.update {
                    it.copy(
                        sharingTop2 = sharingTop2,
                        lostTop2 = lostTop2,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "홈 로딩 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }
}