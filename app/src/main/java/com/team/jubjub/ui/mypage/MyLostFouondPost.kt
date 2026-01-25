package com.team.jubjub.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentMyLostFoundPostBinding
import com.team.jubjub.databinding.PopupMenuLostfoundBinding
import com.team.jubjub.ui.post.PostDetailFragment

class MyLostFoundPost : Fragment(R.layout.fragment_my_lost_found_post) {

    private var _binding: FragmentMyLostFoundPostBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMyLostFoundPostBinding.bind(view)

        setupBackButton()
        setupSearch()
        setupPostClick()
        setupPostMenu()
    }

    private fun setupBackButton() {
        binding.icArrowBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnClickListener { binding.etSearch.requestFocus() }

        binding.icCustomSearch.setOnClickListener {
            val keyword = binding.etSearch.text.toString()
            if (keyword.isBlank()) {
                Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "내 분실/습득글에서 검색: $keyword", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPostClick() {
        val titleViews = listOf(
            binding.tvTitle1, binding.tvTitle2, binding.tvTitle3,
            binding.tvTitle4, binding.tvTitle5, binding.tvTitle6
        )

        titleViews.forEachIndexed { index, tv ->
            tv.setOnClickListener {
                Toast.makeText(requireContext(), "내 분실/습득글 ${index + 1} 상세 이동", Toast.LENGTH_SHORT).show()

                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        // TODO: PostType에 LOST_FOUND가 있으면 그걸로 바꾸기
                        PostDetailFragment.newInstance(PostDetailFragment.PostType.SHARE)
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    /** 메뉴 버튼 클릭 -> 팝업 띄우기 */
    private fun setupPostMenu() {
        val menuButtons = listOf(
            binding.btnMenu1, binding.btnMenu2, binding.btnMenu3,
            binding.btnMenu4, binding.btnMenu5, binding.btnMenu6
        )

        menuButtons.forEachIndexed { index, btn ->
            btn.setOnClickListener { v ->
                showLostFoundMenuPopup(
                    anchor = v,
                    onStatus = {
                        Toast.makeText(requireContext(), "찾기 완료 상태 (글 ${index + 1})", Toast.LENGTH_SHORT).show()
                        // TODO: 여기서 "찾기 완료 처리" 같은 다이얼로그/처리 연결 가능
                    },
                    onEdit = {
                        Toast.makeText(requireContext(), "수정 (글 ${index + 1})", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        Toast.makeText(requireContext(), "삭제 (글 ${index + 1})", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    /** 분실/습득 글 전용 팝업 (찾기 완료 상태 / 수정 / 삭제) */
    private fun showLostFoundMenuPopup(
        anchor: View,
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

        // 누른 메뉴 아이콘 바로 아래에 뜸 (필요하면 xOff만 조정)
        popup.showAsDropDown(anchor, 0, 8)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
