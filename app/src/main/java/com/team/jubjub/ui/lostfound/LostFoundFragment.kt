package com.team.jubjub.ui.lostfound

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
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.databinding.FragmentLostFoundBinding
import com.team.jubjub.ui.home.HomeFragment
import com.team.jubjub.ui.post.PostDetailFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LostFoundFragment : Fragment(R.layout.fragment_lost_found) {

    private var _binding: FragmentLostFoundBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LostFoundViewModel by viewModels()

    private lateinit var lostAdapter: LostAdapter
    private var originList: List<Post> = emptyList()

    private var selectedFilterIndex = 0 // 0: 전체, 1: 찾는 중, 2: 찾음 완료

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLostFoundBinding.bind(view)

        setupBackButton()
        setupRecyclerView()
        setupSearch()
        setupFilter()
        observeViewModel()

        // [삭제됨] 여기서 호출하면 처음에만 로드되고, 뒤로가기 했을 때 갱신이 안 됨
        // viewModel.loadLostPosts("서울여자대학교")
    }

    // ★ [추가] 화면이 다시 보일 때마다 실행되는 함수
    override fun onResume() {
        super.onResume()
        // 여기서 데이터를 다시 불러와야 댓글 수 변경 사항이 반영됨
        viewModel.loadLostPosts("서울여자대학교")
    }

    /**
     * 🔙 백 버튼 → 홈 화면으로 이동
     */
    private fun setupBackButton() {
        binding.icBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
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

            viewModel.searchPosts("서울여자대학교", keyword)
        }
    }

    /**
     * 🎛 필터 다이얼로그 (화이트 배경)
     */
    private fun setupFilter() {
        binding.icFilter.setOnClickListener {
            val filters = arrayOf("전체", "찾는 중", "찾음 완료")

            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.WhiteDialogTheme   // ✅ 화이트 배경 적용
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
            1 -> originList.filter { it.status == PostStatus.AVAILABLE }  // 찾는 중
            2 -> originList.filter { it.status == PostStatus.COMPLETED }  // 찾음 완료
            else -> originList
        }

        lostAdapter = LostAdapter(filteredList) { post ->
            moveToDetail(post)
        }
        binding.rvPost.adapter = lostAdapter
    }

    private fun observeViewModel() {
        viewModel.postList.observe(viewLifecycleOwner) { list ->
            originList = list
            applyFilter(selectedFilterIndex)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun moveToDetail(post: Post) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                PostDetailFragment.newInstance(
<<<<<<< HEAD
                    PostType.SHARING,
                    post.postId
                )
=======
                    PostType.LOST,
                    post.postId)
>>>>>>> 6c5849704163b1b4cd5bb33b28a9ab2fe0ff540a
            )
            .addToBackStack(null)
            .commit()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}