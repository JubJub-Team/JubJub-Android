package com.team.jubjub.ui.post

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.team.jubjub.R
import com.team.jubjub.data.model.Comment
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.model.enums.TradeMethod
import com.team.jubjub.data.repository.PostRepository
import com.team.jubjub.databinding.FragmentPostDetailBinding
import com.team.jubjub.ui.lostfound.LostFoundFragment
import com.team.jubjub.ui.mypage.AlarmFragment
import com.team.jubjub.ui.mypage.MyPageFragment
import com.team.jubjub.ui.share.ShareFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var postRepository: PostRepository

    private lateinit var adapter: DetailAdapter
    private val comments = mutableListOf<Comment>()

    // 현재 게시글 객체 (댓글 작성 시 사용)
    private var currentPost: Post? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 인자 받기 (Enum 타입 안전하게)
        val postType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_POST_TYPE, PostType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_POST_TYPE) as? PostType
        } ?: PostType.LOST

        val postId = arguments?.getString(ARG_POST_ID).orEmpty()

        // 2. 툴바 뒤로가기
        binding.toolBar.setNavigationOnClickListener {
            goBackToBoard(postType)
        }

        // 3. 툴바 메뉴 (알림/프로필)
        binding.toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_alarm -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, AlarmFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.action_profile -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, MyPageFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }
        }

        // 4. 시스템 뒤로가기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goBackToBoard(postType)
        }

        // 5. 실제 데이터 로드 및 UI 표시
        loadPostDetail(postId, postType)

        // 6. 키보드/인셋 처리
        setupKeyboardHandling()
    }

    private fun setupKeyboardHandling() {
        val rv = binding.rvDetail
        val inputBar = binding.commentInputBar

        inputBar.doOnLayout {
            val inputBarHeight = inputBar.height
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
                val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
                inputBar.updatePadding(bottom = ime.bottom)
                rv.updatePadding(bottom = inputBarHeight + ime.bottom)
                insets
            }
            ViewCompat.requestApplyInsets(binding.root)
        }
    }

    private fun loadPostDetail(postId: String, fallbackType: PostType) {
        if (postId.isBlank()) {
            Toast.makeText(requireContext(), "게시글 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            goBackToBoard(fallbackType)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            postRepository.getPostDetail(postId)
                .onSuccess { post ->
                    if (post == null) {
                        Toast.makeText(requireContext(), "삭제되었거나 없는 게시글입니다.", Toast.LENGTH_SHORT).show()
                        goBackToBoard(fallbackType)
                        return@onSuccess
                    }
                    currentPost = post

                    val header = makeHeaderFromPost(post)
                    setupAdapter(header)

                    // 댓글 데이터 로드
                    loadComments(post.postId)
                }
                .onFailure { e ->
                    Toast.makeText(requireContext(), e.message ?: "상세 조회 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadComments(postId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            postRepository.getCommentList(postId)
                .onSuccess { list ->
                    comments.clear()
                    comments.addAll(list)
                    if (::adapter.isInitialized) {
                        adapter.notifyDataSetChanged()
                    }
                }
        }
    }

    private fun makeHeaderFromPost(post: Post): DetailHeader {
        val idDate = "${post.writerCustomId} • ${formatTs(post.createdAt)}"

        // ★ [수정] images 리스트의 첫 번째를 썸네일로 사용
        val imageUrl = post.images.firstOrNull()

        return when (post.postType) {
            PostType.SHARING -> {
                DetailHeader.Share(
                    idDate = idDate,
                    title = post.title,
                    category = post.category?.name ?: "카테고리 없음",
                    condition = post.productCondition?.name ?: "상태 없음",
                    count = (post.quantity ?: 1).toString(),
                    content = post.content,
                    deliveryEnabled = post.tradeMethods?.contains(TradeMethod.DELIVERY) == true,
                    directEnabled = post.tradeMethods?.contains(TradeMethod.DIRECT) == true,
                    location = post.hopeLocation ?: "장소 미정",
                    imageUrl = imageUrl // 이미지 URL 전달
                )
            }
            PostType.LOST -> {
                DetailHeader.LostFound(
                    idDate = idDate,
                    title = post.title,
                    foundPlace = post.foundLocation ?: "장소 미정",
                    detailPlace = post.foundDetailLocation ?: "",
                    foundDate = formatTs(post.foundDate),
                    content = post.content,
                    entrustedPlace = post.storageLocation ?: "보관 장소 없음",
                    imageUrl = imageUrl // 이미지 URL 전달
                )
            }
        }
    }

    private fun setupAdapter(header: DetailHeader) {
        adapter = DetailAdapter(header, comments)
        binding.rvDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDetail.adapter = adapter

        // 댓글 전송 버튼
        binding.btnSend.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            val post = currentPost
            if (text.isNotEmpty() && post != null) {
                // TODO: 내 정보(프로필, 아이디) 로드 필요. 현재는 더미 데이터
                val newComment = Comment(
                    writerUserId = "my_uid_test",
                    writerNickname = "나",
                    writerProfileImageUrl = "", // 내 프로필 이미지
                    content = text,
                    createdAt = java.util.Date()
                )

                // 화면 즉시 반영
                adapter.addComment(newComment)
                binding.etComment.setText("")
                binding.rvDetail.scrollToPosition(adapter.itemCount - 1)

                // 서버 전송
                viewLifecycleOwner.lifecycleScope.launch {
                    postRepository.addComment(post.postId, newComment, post.writerUserId)
                }
            }
        }
    }

    private fun formatTs(ts: com.google.firebase.Timestamp?): String {
        if (ts == null) return ""
        val date = ts.toDate()
        val cal = Calendar.getInstance().apply { time = date }
        val mm = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val dd = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val hh = cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val mi = cal.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "$mm/$dd $hh:$mi"
    }

    private fun goBackToBoard(postType: PostType) {
        val targetFragment = when (postType) {
            PostType.SHARING -> ShareFragment()
            PostType.LOST -> LostFoundFragment()
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, targetFragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_POST_TYPE = "arg_post_type"
        private const val ARG_POST_ID = "arg_post_id"

        fun newInstance(type: PostType, postId: String): PostDetailFragment =
            PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_POST_TYPE, type)
                    putString(ARG_POST_ID, postId)
                }
            }
    }
}