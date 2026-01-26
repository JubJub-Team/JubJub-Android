package com.team.jubjub.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentMyScrapBinding
import com.team.jubjub.ui.post.PostDetailFragment

class MyScrapFragment : Fragment(R.layout.fragment_my_scrap) {

    private var _binding: FragmentMyScrapBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyScrapBinding.bind(view)

        setupBackButton()
        setupSearch()
        setupPostClick()
    }

    private fun setupBackButton() {
        binding.icArrowBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnClickListener { binding.etSearch.requestFocus() }

        binding.icCustomSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString()
            if (keyword.isBlank()) {
                Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "내 스크랩에서 검색: $keyword", Toast.LENGTH_SHORT).show()
                // TODO: 스크랩 검색 로직/API 연동
            }
        }
    }

    private fun setupPostClick() {
        val titleViews = listOf(
            binding.tvTitle1,
            binding.tvTitle2,
            binding.tvTitle3,
            binding.tvTitle4,
            binding.tvTitle5,
            binding.tvTitle6
        )

        titleViews.forEachIndexed { index, tv ->
            tv.setOnClickListener {
                Toast.makeText(requireContext(), "내 스크랩 ${index + 1} 상세 이동", Toast.LENGTH_SHORT).show()

                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        PostDetailFragment.newInstance(PostDetailFragment.PostType.SHARE)
                        // TODO: 스크랩이 나눔 글만이 아니라면 타입을 post마다 다르게 넘겨야 함
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
