package com.team.jubjub.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentPostDetailBinding
import com.team.jubjub.ui.mypage.AlarmFragment
import com.team.jubjub.ui.lostfound.LostFoundFragment
import com.team.jubjub.ui.mypage.MyPageFragment
import com.team.jubjub.ui.share.ShareFragment

class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

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

        //PostType 받기
        val postType = (arguments?.getSerializable(ARG_POST_TYPE) as? PostType) ?: PostType.SHARE

        //뒤로가기
        binding.toolBar.setNavigationOnClickListener {
            goBackToBoard(postType)
        }

        //알림/프로필 이동
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

        //시스템 뒤로가기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goBackToBoard(postType)
        }
        val header: DetailHeader = when (postType) {
            PostType.SHARE -> DetailHeader.Share(
                idDate = "글쓴 사람 아이디•01/17 1:05",
                title = "게시글 제목",
                category = "책",
                condition = "새 상품(미사용)",
                count = "1",
                content = "게시글 내용이 길어지면 자동 줄바꿈\n여러 줄도 가능",
                deliveryEnabled = true,
                directEnabled = true,
                location = "서울여대 과학관"
            )

            PostType.LOST_FOUND -> DetailHeader.LostFound(
                idDate = "글쓴 사람 아이디•01/17 1:05",
                title = "분실물 게시글 제목",
                foundPlace = "서울여대 과학관",
                detailPlace = "제 2과-302",
                foundDate = "01/17 12:35",
                content = "게시글 내용",
                entrustedPlace = "누리관 2층 학생지원실"
            )
        }

        comments.clear()
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

    //게시판 돌아기기 함수
    private fun goBackToBoard(postType: PostType) {
        when (postType) {
            PostType.SHARE -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ShareFragment())
                    .commit()
            }

            PostType.LOST_FOUND -> {
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

    enum class PostType {
        SHARE, LOST_FOUND
    }

    companion object {
        private const val ARG_POST_TYPE = "arg_post_type"

        fun newInstance(type: PostType): PostDetailFragment =
            PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_POST_TYPE, type)
                }
            }
    }
}
