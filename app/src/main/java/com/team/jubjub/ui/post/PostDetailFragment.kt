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

        val postType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_POST_TYPE, PostType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(ARG_POST_TYPE) as? PostType
        } ?: PostType.LOST // 기본값 설정

        val postId = arguments?.getString(ARG_POST_ID).orEmpty()

        // 뒤로가기(툴바)
        binding.toolBar.setNavigationOnClickListener {
            goBackToBoard(postType)
        }

        // 알림/프로필 이동
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

        // 시스템 뒤로가기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goBackToBoard(postType)
        }

        // 실제 상세 로드 (3,4번 핵심)
        loadPostDetail(postId, postType)

        // 키보드/인셋 처리(기존 그대로)
        val rv = binding.rvDetail
        val inputBar = binding.commentInputBar

        inputBar.doOnLayout {
            val inputBarHeight = inputBar.height

            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
                val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

                // 키보드 올라오면 입력바를 키보드 위로 배치
                inputBar.updatePadding(bottom = ime.bottom)

                // 마지막 리스트 안 가려지도록 패딩
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
                        return@launch
                    }

                    val header = makeHeaderFromPost(post)
                    setupAdapter(header)

                    // TODO(선택): 실제 댓글 연결
                    // loadComments(post.postId)
                }
                .onFailure { e ->
                    Toast.makeText(requireContext(), e.message ?: "상세 조회 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun makeHeaderFromPost(post: Post): DetailHeader {
        val idDate = "${post.writerCustomId}•${formatTs(post.createdAt)}"

        return when (post.postType) {
            PostType.SHARING -> {
                DetailHeader.Share(
                    idDate = idDate,
                    title = post.title,
                    category = post.category?.name ?: "",
                    condition = post.productCondition?.name ?: "",
                    count = (post.quantity ?: 1).toString(),
                    content = post.content,
                    deliveryEnabled = post.tradeMethods?.contains(TradeMethod.DELIVERY) == true,
                    directEnabled = post.tradeMethods?.contains(TradeMethod.DIRECT) == true,
                    location = post.hopeLocation ?: "장소 미정"
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
                    entrustedPlace = post.storageLocation ?: ""
                )
            }
        }
    }

    private fun setupAdapter(header: DetailHeader) {
        comments.clear()

        // 더미 댓글
        comments.add(Comment("닉네임", "01/18 07:54", "저요저요"))
        comments.add(Comment("닉네임", "01/18 07:55", "네 어떻게 드릴까요?", isReply = true))

        adapter = DetailAdapter(header, comments)
        binding.rvDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDetail.adapter = adapter

        binding.btnSend.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                adapter.addComment(Comment("나", "방금", text))
                binding.etComment.setText("")
                binding.rvDetail.scrollToPosition(adapter.itemCount - 1)
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

    // 게시판 돌아가기
    private fun goBackToBoard(postType: PostType) {
        when (postType) {
            PostType.SHARING -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ShareFragment())
                    .commit()
            }
            PostType.LOST -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, LostFoundFragment())
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_POST_TYPE = "arg_post_type"
        private const val ARG_POST_ID = "arg_post_id"

        //데이터 enum + postId를 그대로 받음
        fun newInstance(type: PostType, postId: String): PostDetailFragment =
            PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_POST_TYPE, type)
                    putString(ARG_POST_ID, postId)
                }
            }
    }
}
