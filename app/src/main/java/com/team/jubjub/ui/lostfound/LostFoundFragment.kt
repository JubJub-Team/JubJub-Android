package com.team.jubjub.ui.lostfound

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentLostFoundBinding
import com.team.jubjub.ui.post.PostDetailFragment

class LostFoundFragment : Fragment(R.layout.fragment_lost_found) {

    private var _binding: FragmentLostFoundBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLostFoundBinding.bind(view)

        setupBackButton()
        setupSearch()
        setupFilter()
        setupPostClick()
    }

    /* ------------------------
       상단바 뒤로가기
    ------------------------ */
    private fun setupBackButton() {
        binding.icArrowBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    /* ------------------------
       검색
    ------------------------ */
    private fun setupSearch() {

        // 검색창 클릭 시 → 포커스 + 키보드
        binding.etSearch.setOnClickListener {
            binding.etSearch.requestFocus()
        }

        // 검색 아이콘 클릭
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
       필터
    ------------------------ */
    private fun setupFilter() {
        binding.icFilter.setOnClickListener {
            val filters = arrayOf("찾는 중만 보기", "찾음 완료만 보기")

            MaterialAlertDialogBuilder(requireContext())
                .setItems(filters) { _, which ->
                    when (which) {
                        0 -> Toast.makeText(requireContext(), "찾는 중", Toast.LENGTH_SHORT).show()
                        1 -> Toast.makeText(requireContext(), "찾음 완료", Toast.LENGTH_SHORT).show()
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
                    PostDetailFragment.newInstance(PostDetailFragment.PostType.LOST_FOUND)
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
