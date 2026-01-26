package com.team.jubjub.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.team.jubjub.ConfirmDialogFragment
import com.team.jubjub.MainActivity
import com.team.jubjub.R
import com.team.jubjub.data.model.enums.UserLevel
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.databinding.FragmentMyPageBinding
import com.team.jubjub.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyPageFragment : Fragment() {

    @Inject lateinit var authRepository: AuthRepository

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    private val REQ_POST_TYPE = "req_post_type"
    private val REQ_LOGOUT = "req_logout"
    private val REQ_WITHDRAW = "req_withdraw"

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

        // TODO: 서버/로컬에서 받아온 레벨 값으로 교체
        val levelInt = 2
        val userLevel = UserLevel.from(levelInt)
        binding.ivLevelBar.setImageResource(userLevel.levelBarRes)

        // -----------------------------
        // 내 게시물 종류 선택 다이얼로그 결과
        // -----------------------------
        parentFragmentManager.setFragmentResultListener(REQ_POST_TYPE, viewLifecycleOwner) { _, bundle ->
            val choice = ConfirmDialogFragment.readChoice(bundle)
            when (choice) {
                DialogChoice.LEFT -> openMyLostFoundPost()
                DialogChoice.RIGHT -> openMySharePost()
            }
        }

        binding.tvItemMyPosts.setOnClickListener {
            showPostTypeDialog()
        }

        // -----------------------------
        // 프로필/알림 등 네비게이션
        // -----------------------------
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

        binding.tvItemLiked.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MyScrapFragment())
                .addToBackStack(null)
                .commit()
        }

        // -----------------------------
        // 로그아웃 다이얼로그 결과
        // -----------------------------
        parentFragmentManager.setFragmentResultListener(REQ_LOGOUT, viewLifecycleOwner) { _, bundle ->
            val choice = ConfirmDialogFragment.readChoice(bundle)
            if (choice == DialogChoice.LEFT) {
                doLogout()
            }
        }

        binding.tvItemLogout.setOnClickListener {
            ConfirmDialogFragment
                .newInstance(REQ_LOGOUT, ConfirmDialogSpec.Logout.key)
                .show(parentFragmentManager, "confirm_logout")
        }

        // -----------------------------
        // 회원탈퇴 다이얼로그 결과
        // -----------------------------
        parentFragmentManager.setFragmentResultListener(REQ_WITHDRAW, viewLifecycleOwner) { _, bundle ->
            val choice = ConfirmDialogFragment.readChoice(bundle)
            if (choice == DialogChoice.LEFT) {
                withdrawMember()
            }
        }

        binding.tvItemWithdraw.setOnClickListener {
            ConfirmDialogFragment
                .newInstance(REQ_WITHDRAW, ConfirmDialogSpec.Withdraw.key)
                .show(parentFragmentManager, "confirm_withdraw")
        }
    }

    private fun doLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            authRepository.signOut() // 인스턴스로 호출
                .onSuccess { _: Unit ->
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish()
                }
                .onFailure { e: Throwable ->
                    Toast.makeText(requireContext(), "로그아웃 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
            specKey = ConfirmDialogSpec.PostType.key
        ).show(parentFragmentManager, "postTypeDialog")
    }

    private fun withdrawMember() {
        // TODO: 실제 탈퇴 처리 연결 (authRepository.withdrawAccount() 등)
        // 성공 시: LoginActivity로 CLEAR_TASK 이동
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
