package com.team.jubjub.ui.write

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostCategory
import com.team.jubjub.data.model.enums.PostStatus
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.model.enums.ProductCondition
import com.team.jubjub.data.model.enums.TradeMethod
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.ImageUploadRepository
import com.team.jubjub.data.repository.PostRepository
import com.team.jubjub.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WriteShareViewModel @Inject constructor(
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

    fun uploadSharingPostWithImage(
        title: String,
        content: String,
        categoryText: String,
        conditionText: String,
        quantity: Int?,
        methodTexts: List<String>,
        hopeLocation: String?,
        imageUri: Uri?,
        keywords: List<String> = emptyList()
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
        if (quantity != null && quantity <= 0) {
            viewModelScope.launch { _event.send(Event.Validation("수량은 1 이상이어야 해요.")) }
            return
        }

        val uid = authRepository.getCurrentUserUid()
        if (uid.isNullOrBlank()) {
            viewModelScope.launch { _event.send(Event.Fail("로그인 필요")) }
            return
        }

        viewModelScope.launch {
            val user = userRepository.getUserProfile(uid)
                .getOrElse {
                    _event.send(Event.Fail("유저 정보 조회 실패: ${it.message}"))
                    return@launch
                }

            if (user.school.isBlank()) {
                _event.send(Event.Fail("유저 학교 정보 없음"))
                return@launch
            }

            // 이미지 업로드(선택한 경우만)
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

            // 문자열 -> enum 매핑 (확정 버전)
            val categoryEnum: PostCategory? =
                categoryText.trim().takeIf { it.isNotBlank() }?.let { mapCategory(it) }

            val conditionEnum: ProductCondition? =
                conditionText.trim().takeIf { it.isNotBlank() }?.let { mapCondition(it) }

            val tradeMethodEnums: List<TradeMethod>? =
                methodTexts
                    .mapNotNull { mapTradeMethod(it) }
                    .distinct()
                    .takeIf { it.isNotEmpty() }

            val post = Post(
                postType = PostType.SHARING,
                school = user.school,

                writerUserId = uid,
                writerCustomId = user.customId,
                writerNickname = user.nickname,
                writerProfileImageUrl = user.profileImageUrl,

                title = t,
                content = c,
                images = imageUrls,
                status = PostStatus.AVAILABLE,

                keywords = keywords,

                //나눔 전용 필드
                category = categoryEnum,
                productCondition = conditionEnum,
                quantity = quantity ?: 1,
                tradeMethods = tradeMethodEnums,
                hopeLocation = hopeLocation?.trim().takeIf { !it.isNullOrBlank() }
            )

            postRepository.uploadPost(post)
                .onSuccess { _event.send(Event.UploadSuccess) }
                .onFailure { _event.send(Event.Fail("업로드 실패: ${it.message}")) }
        }
    }

    private fun mapCategory(label: String): PostCategory? {
        return when (label) {
            "의류" -> PostCategory.CLOTHING
            "식품" -> PostCategory.FOOD
            "전자기기" -> PostCategory.ELECTRONICS
            "도서" -> PostCategory.BOOK
            "기타" -> PostCategory.OTHERS
            else -> null
        }
    }

    private fun mapCondition(label: String): ProductCondition? {
        return when (label) {
            "새상품" -> ProductCondition.NEW
            "사용감 없음" -> ProductCondition.LIKE_NEW
            "사용감 적음" -> ProductCondition.LIGHT_USE
            "사용감 많음" -> ProductCondition.HEAVY_USE
            "고장 및 파손" -> ProductCondition.DAMAGED
            else -> null
        }
    }

    private fun mapTradeMethod(label: String): TradeMethod? {
        return when (label) {
            "직거래" -> TradeMethod.DIRECT
            "택배" -> TradeMethod.DELIVERY
            "사물함" -> TradeMethod.LOCKER
            else -> null
        }
    }
}
