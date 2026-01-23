package com.team.jubjub.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.team.jubjub.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupSearch()

        return binding.root
    }

    private fun setupSearch() {
        // 키보드에서 "검색" 눌렀을 시 액션
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // 오른쪽 돋보기 아이콘 클릭
        binding.ivSearch.setOnClickListener {
            performSearch()
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isEmpty()) return

        // TODO: 여기서 실제 검색 처리
        // 예) ViewModel 호출, 검색 화면 이동 등
        // findNavController().navigate(...)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}