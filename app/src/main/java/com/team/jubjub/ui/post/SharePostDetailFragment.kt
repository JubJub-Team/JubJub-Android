package com.team.jubjub.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.team.jubjub.databinding.FragmentSharePostDetailBinding

class SharePostDetailFragment : Fragment() {

    private var _binding: FragmentSharePostDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DetailAdapter
    private val comments = mutableListOf<Comment>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSharePostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val header = PostHeader(
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
