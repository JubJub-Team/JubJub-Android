package com.team.jubjub.ui.lostfound

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.team.jubjub.R
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.databinding.FragmentLostFoundBinding
import com.team.jubjub.ui.post.PostDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import com.team.jubjub.MainActivity

@AndroidEntryPoint
class LostFoundFragment : Fragment(R.layout.fragment_lost_found) {

    private var _binding: FragmentLostFoundBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LostFoundViewModel by viewModels()

    private lateinit var lostAdapter: LostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLostFoundBinding.bind(view)

        setupBackButton()
        setupRecyclerView()
        setupSearch()
        setupFilter()
        observeViewModel()

        viewModel.loadLostPosts()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadLostPosts()
    }

    /**
     * 백 버튼 → 홈 화면으로 이동
     */
    private fun setupBackButton() {
        binding.icBack.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_home)
        }
    }

    private fun setupRecyclerView() {
        lostAdapter = LostAdapter(emptyList()) { post ->
            moveToDetail(post)
        }

        binding.rvPost.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lostAdapter
        }
    }

    private fun setupSearch() {
        binding.icCustomSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString()

            if (keyword.isBlank()) {
                Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.searchPosts(keyword)
        }
    }

    /**
     * 필터 (바텀 시트 적용)
     */
    private fun setupFilter() {
        binding.icFilter.setOnClickListener {
            val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.filter_lost_found, null)
            bottomSheet.setContentView(view)

            // 전체
            view.findViewById<View>(R.id.tv_filter_all).setOnClickListener {
                viewModel.filterByStatus(0)
                bottomSheet.dismiss()
            }
            // 찾는 중
            view.findViewById<View>(R.id.tv_filter_finding).setOnClickListener {
                viewModel.filterByStatus(1)
                bottomSheet.dismiss()
            }
            // 찾음 완료
            view.findViewById<View>(R.id.tv_filter_found).setOnClickListener {
                viewModel.filterByStatus(2)
                bottomSheet.dismiss()
            }
            view.findViewById<View>(R.id.tv_filter_with_location).setOnClickListener {
                viewModel.filterByStatus(3)
                bottomSheet.dismiss()
            }

            bottomSheet.show()
        }
    }

    private fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner) { list ->
            lostAdapter = LostAdapter(list) { moveToDetail(it) }
            binding.rvPost.adapter = lostAdapter
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun moveToDetail(post: Post) {
        val detailFragment = PostDetailFragment.newInstance(
            type = PostType.LOST,
            postId = post.postId
        )

        (requireActivity() as MainActivity).openOverlay(detailFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
