package com.team.jubjub.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.team.jubjub.R
import com.team.jubjub.data.model.Post
import com.team.jubjub.databinding.FragmentMyLostFoundPostBinding
import com.team.jubjub.databinding.PopupMenuLostfoundBinding
import com.team.jubjub.ui.post.PostDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts


@AndroidEntryPoint
class MyLostFoundPostFragment : Fragment(R.layout.fragment_my_lost_found_post) {

    private var _binding: FragmentMyLostFoundPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyLostFoundPostViewModel by viewModels()
    private lateinit var adapter: MyPostAdapter
    private var selectedImageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyLostFoundPostBinding.bind(view)

        setupBackButton()
        setupSearch()
        setupRecyclerView()
        observe()

        viewModel.loadMyLostPosts()
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
            onMenuClick = { anchor, post ->
                showLostFoundMenuPopup(
                    anchor = anchor,
                    post = post,
                    onStatus = {
                        Toast.makeText(requireContext(), "찾기 완료 상태: ${post.postId}", Toast.LENGTH_SHORT).show()
                        // TODO: status 업데이트 연결 (예: updatePostStatus)
                    },
                    onEdit = {
                        Toast.makeText(requireContext(), "수정: ${post.postId}", Toast.LENGTH_SHORT).show()
                        // TODO: 수정 화면 이동
                    },
                    onDelete = {
                        Toast.makeText(requireContext(), "삭제: ${post.postId}", Toast.LENGTH_SHORT).show()
                        // TODO: deletePost 연결
                    }
                )
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

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            selectedImageUri = uri
            Toast.makeText(requireContext(), "사진 1장 선택됨", Toast.LENGTH_SHORT).show()
        }

    private fun showLostFoundMenuPopup(
        anchor: View,
        post: Post,
        onStatus: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ) {
        val b = PopupMenuLostfoundBinding.inflate(LayoutInflater.from(requireContext()))

        val popup = PopupWindow(
            b.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 16f
            isOutsideTouchable = true
        }

        b.tvStatus.setOnClickListener { popup.dismiss(); onStatus() }
        b.tvEdit.setOnClickListener { popup.dismiss(); onEdit() }
        b.tvDelete.setOnClickListener { popup.dismiss(); onDelete() }

        popup.showAsDropDown(anchor, 0, 8)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
