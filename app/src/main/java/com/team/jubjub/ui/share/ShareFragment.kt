package com.team.jubjub.ui.share

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentShareBinding
import com.team.jubjub.ui.post.PostDetailFragment

class ShareFragment : Fragment(R.layout.fragment_share) {

    private var _binding: FragmentShareBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentShareBinding.bind(view)

        setupBackButton()
        setupSearch()
        setupFilter()
        setupPostClick()
    }

    /* ------------------------
       상단바 뒤로가기
    ------------------------ */
    private fun setupBackButton() {
        binding.icBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    /* ------------------------
       검색
    ------------------------ */
    private fun setupSearch() {

        binding.etSearch.setOnClickListener {
            binding.etSearch.requestFocus()
        }

        binding.icCustomSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString()

            if (keyword.isBlank()) {
                Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "검색어: $keyword", Toast.LENGTH_SHORT).show()
                // TODO: 검색 API 연동
            }
        }
    }

    /* ------------------------
       필터 (나눔)
    ------------------------ */
    private fun setupFilter() {
        binding.icFilter.setOnClickListener {
            val filters = arrayOf("나눔 중", "예약 중", "나눔 완료")

            MaterialAlertDialogBuilder(requireContext())
                .setItems(filters) { _, which ->
                    when (which) {
                        0 -> Toast.makeText(requireContext(), "나눔 중", Toast.LENGTH_SHORT).show()
                        1 -> Toast.makeText(requireContext(), "예약 중", Toast.LENGTH_SHORT).show()
                        2 -> Toast.makeText(requireContext(), "나눔 완료", Toast.LENGTH_SHORT).show()
                    }
                    // TODO: 필터 API 연동
                }
                .show()
        }
    }

    /* ------------------------
       게시글 클릭
    ------------------------ */
    private fun setupPostClick() {
        binding.tvTitle1.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    PostDetailFragment.newInstance(PostDetailFragment.PostType.SHARE)
                )
                .addToBackStack(null)
                .commit()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
