package com.team.jubjub.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.team.jubjub.ConfirmDialogFragment
import com.team.jubjub.MainActivity
import com.team.jubjub.R
import com.team.jubjub.data.model.enums.UserLevel
import com.team.jubjub.databinding.FragmentMyPageBinding

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!
    private val REQ_POST_TYPE = "req_post_type"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val levelInt = 2 // TODO: 서버/로컬에서 받아온 레벨 값으로 교체
        val userLevel = UserLevel.from(levelInt)
        val REQ_LOGOUT = "req_logout"
        val REQ_WITHDRAW = "req_withdraw"

        binding.ivLevelBar.setImageResource(userLevel.levelBarRes)


        // 결과 리스너
        parentFragmentManager.setFragmentResultListener(
            REQ_POST_TYPE,
            viewLifecycleOwner
        ) { _, bundle ->
            val choice = ConfirmDialogFragment.readChoice(bundle)

            when (choice) {
                DialogChoice.LEFT -> {   // LEFT = "분실물"
                    openMyLostFoundPost()
                }
                DialogChoice.RIGHT -> {  // RIGHT = "나눔"
                    openMySharePost()
                }
            }
        }

        // 클릭 -> 다이얼로그
        binding.tvItemMyPosts.setOnClickListener {
            showPostTypeDialog()
        }

        binding.ivGoToProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tvItemAlarm.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AlarmFragment())
                .addToBackStack(null)
                .commit()
        }

        parentFragmentManager.setFragmentResultListener(REQ_LOGOUT, viewLifecycleOwner) { _, bundle ->
            val choice = ConfirmDialogFragment.readChoice(bundle)
            if (choice == DialogChoice.LEFT) {
                // TODO 로그아웃 처리
            }
        }

        binding.tvItemLogout.setOnClickListener {
            ConfirmDialogFragment
                .newInstance(REQ_LOGOUT, ConfirmDialogSpec.Logout.key)
                .show(parentFragmentManager, "confirm_logout")
        }

        parentFragmentManager.setFragmentResultListener(REQ_WITHDRAW, viewLifecycleOwner) { _, bundle ->
            val choice = ConfirmDialogFragment.readChoice(bundle)
            if (choice == DialogChoice.LEFT) {
                //"예" 눌렀을 때 회원탈퇴 실행
                withdrawMember()
            }
        }

        // 버튼 클릭 -> 다이얼로그 show
        binding.tvItemWithdraw.setOnClickListener {
            ConfirmDialogFragment
                .newInstance(REQ_WITHDRAW, ConfirmDialogSpec.Withdraw.key)
                .show(parentFragmentManager, "confirm_withdraw")
        }

        binding.tvItemLiked.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MyScrapFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun openMySharePost() {
        (activity as? MainActivity)?.openOverlay(MySharePost(), "MySharePost")
    }

    private fun openMyLostFoundPost() {
        (activity as? MainActivity)?.openOverlay(MyLostFoundPost(), "MyLostFoundPost")
    }

    private fun showPostTypeDialog() {
        ConfirmDialogFragment.newInstance(
            requestKey = REQ_POST_TYPE,
            specKey = ConfirmDialogSpec.PostType.key // "postType"
        ).show(parentFragmentManager, "postTypeDialog")
    }

    fun withdrawMember() {
        // TODO: 여기에 실제 탈퇴 처리 연결
        // 예) viewModel.withdraw()
        // 성공 시: 로그인 화면으로 이동, 토큰 삭제 등
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
