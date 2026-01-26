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
import com.team.jubjub.databinding.FragmentMySharePostBinding
import com.team.jubjub.ui.post.PostDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MySharePostFragment : Fragment(R.layout.fragment_my_share_post) {

    private var _binding: FragmentMySharePostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MySharePostViewModel by viewModels()
    private lateinit var adapter: MyPostAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMySharePostBinding.bind(view)

        setupBackButton()
        setupSearch()
        setupRecyclerView()
        observe()

        viewModel.loadMySharePosts()
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
                viewModel.search(keyword)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = MyPostAdapter(
            onItemClick = { post ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        PostDetailFragment.newInstance(post.postType, post.postId)
                    )
                    .addToBackStack(null)
                    .commit()
            },
            onMenuClick = { _, post ->
                // MySharePost도 메뉴 필요하면 여기서 팝업 연결하면 됨
                Toast.makeText(requireContext(), "메뉴: ${post.postId}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosts.adapter = adapter
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.posts.collect { list ->
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
