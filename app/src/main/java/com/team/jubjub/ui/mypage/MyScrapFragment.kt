package com.team.jubjub.ui.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentMyScrapBinding
import com.team.jubjub.ui.post.PostDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyScrapFragment : Fragment(R.layout.fragment_my_scrap) {

    private var _binding: FragmentMyScrapBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MyScrapAdapter
    private val viewModel: MyScrapViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyScrapBinding.bind(view)

        setupBackButton()
        setupSearch()
        setupRecyclerView()
        observe()

        // 여기서 실제 스크랩 로드
        viewModel.loadScraps()
    }

    private fun setupBackButton() {
        binding.icArrowBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnClickListener { binding.etSearch.requestFocus() }

        binding.icCustomSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString().trim()
            if (keyword.isBlank()) {
                Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "내 스크랩에서 검색: $keyword", Toast.LENGTH_SHORT).show()
                // TODO: 스크랩 목록 내 필터링 or 서버 검색
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = MyScrapAdapter { item ->
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    PostDetailFragment.newInstance(item.postType, item.postId)
                )
                .addToBackStack(null)
                .commit()
        }

        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.items.collect { list ->
                        adapter.submitList(list)
                    }
                }
                launch {
                    viewModel.error.collect { msg ->
                        if (!msg.isNullOrBlank()) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
