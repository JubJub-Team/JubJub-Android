package com.team.jubjub.ui.share

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.team.jubjub.R
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.databinding.FragmentShareBinding
import com.team.jubjub.ui.post.PostDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import com.team.jubjub.MainActivity

@AndroidEntryPoint
class ShareFragment : Fragment(R.layout.fragment_share) {

    private var _binding: FragmentShareBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShareViewModel by viewModels()
    private lateinit var shareAdapter: ShareAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentShareBinding.bind(view)

        setupBackButton()
        setupRecyclerView()
        setupSearch()
        setupFilter()
        observeViewModel()

        viewModel.loadSharePosts()
    }

    override fun onResume() {
        super.onResume()

        viewModel.loadSharePosts()
    }

    private fun setupBackButton() {
        binding.icBack.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_home)
        }
    }

    private fun setupRecyclerView() {
        shareAdapter = ShareAdapter(emptyList()) { post ->
            moveToDetail(post)
        }

        binding.rvPost.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shareAdapter
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

    private fun setupFilter() {
        binding.icFilter.setOnClickListener {
            val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.filter_share, null)
            bottomSheet.setContentView(view)

            view.findViewById<View>(R.id.tv_filter_all).setOnClickListener {
                viewModel.filterByStatus(0)
                bottomSheet.dismiss()
            }
            view.findViewById<View>(R.id.tv_filter_available).setOnClickListener {
                viewModel.filterByStatus(1)
                bottomSheet.dismiss()
            }
            view.findViewById<View>(R.id.tv_filter_reserved).setOnClickListener {
                viewModel.filterByStatus(2)
                bottomSheet.dismiss()
            }
            view.findViewById<View>(R.id.tv_filter_completed).setOnClickListener {
                viewModel.filterByStatus(3)
                bottomSheet.dismiss()
            }
            view.findViewById<View>(R.id.tv_filter_with_location).setOnClickListener {
                viewModel.filterByStatus(4)
                bottomSheet.dismiss()
            }
            bottomSheet.show()
        }
    }

    private fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner) { list ->
            shareAdapter.updateList(list)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun moveToDetail(post: Post) {
        val detailFragment = PostDetailFragment.newInstance(
            type = PostType.SHARING,
            postId = post.postId
        )
        (requireActivity() as MainActivity).openOverlay(detailFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
