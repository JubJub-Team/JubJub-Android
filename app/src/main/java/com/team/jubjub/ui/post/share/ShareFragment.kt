package com.team.jubjub.ui.share

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.team.jubjub.R
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.data.model.enums.PostStatus
import com.team.jubjub.databinding.FragmentShareBinding
import com.team.jubjub.ui.home.HomeFragment
import com.team.jubjub.ui.post.PostDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log

@AndroidEntryPoint
class ShareFragment : Fragment(R.layout.fragment_share) {

    private var _binding: FragmentShareBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShareViewModel by viewModels()

    private lateinit var shareAdapter: ShareAdapter
    private var originList: List<Post> = emptyList()

    private var selectedFilterIndex = 0 // 0: 전체, 1: 나눔 중, 2: 예약 중, 3: 나눔 완료

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentShareBinding.bind(view)

        setupBackButton()
        setupRecyclerView()
        setupSearch()
        setupFilter()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSharePosts("서울여자대학교")
    }

    /**
     * 🔙 백 버튼 → 홈 화면
     */
    private fun setupBackButton() {
        binding.icBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }

    /* ------------------------
       RecyclerView
    ------------------------ */
    private fun setupRecyclerView() {
        shareAdapter = ShareAdapter(emptyList()) { post ->
            moveToDetail(post)
        }

        binding.rvPost.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shareAdapter
        }
    }

    /* ------------------------
       검색
    ------------------------ */
    private fun setupSearch() {
        binding.icCustomSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString()

            if (keyword.isBlank()) {
                Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.searchPosts("서울여자대학교", keyword)
        }
    }

    /* ------------------------
       필터 (화이트 배경)
    ------------------------ */
    private fun setupFilter() {
        binding.icFilter.setOnClickListener {
            val filters = arrayOf("전체", "나눔 중", "예약 중", "나눔 완료")

            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.WhiteDialogTheme
            )
                .setItems(filters) { _, which ->
                    selectedFilterIndex = which
                    applyFilter(which)
                }
                .show()
        }
    }

    private fun applyFilter(filterIndex: Int) {
        val filteredList = when (filterIndex) {
            1 -> originList.filter { it.status == PostStatus.AVAILABLE }
            2 -> originList.filter { it.status == PostStatus.RESERVED }
            3 -> originList.filter { it.status == PostStatus.COMPLETED }
            else -> originList
        }

        shareAdapter = ShareAdapter(filteredList) { post ->
            moveToDetail(post)
        }
        binding.rvPost.adapter = shareAdapter
    }

    /* ------------------------
       ViewModel Observe
    ------------------------ */
    private fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner) { list ->
            Log.d("ShareFragment", "받아온 게시물 수: ${list.size}")

            originList = list
            applyFilter(selectedFilterIndex)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    /* ------------------------
       상세 화면 이동
    ------------------------ */
    private fun moveToDetail(post: Post) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                PostDetailFragment.newInstance(
                    PostType.SHARING,
                    post.postId)
            )
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
