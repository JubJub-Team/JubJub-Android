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
import com.team.jubjub.data.repository.UserRepository
import com.team.jubjub.databinding.FragmentMyPageBinding
import com.team.jubjub.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyPageFragment : Fragment() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var userRepository: UserRepository

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!
    private val REQ_POST_TYPE = "req_post_type"
    private val REQ_LOGOUT = "req_logout"
    private val REQ_WITHDRAW = "req_withdraw"

    companion object {
        private const val REQ_POST_TYPE = "req_post_type"
        private const val REQ_LOGOUT = "req_logout"
        private const val REQ_WITHDRAW = "req_withdraw"
    }

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

        // -----------------------------
        // FragmentResult listeners (1회만 등록)
        // -----------------------------
        parentFragmentManager.setFragmentResultListener(REQ_POST_TYPE, viewLifecycleOwner) { _, bundle ->
            when (ConfirmDialogFragment.readChoice(bundle)) {
                DialogChoice.LEFT -> openMyLostFoundPost()
                DialogChoice.RIGHT -> openMySharePost()
            }
        }

        parentFragmentManager.setFragmentResultListener(REQ_LOGOUT, viewLifecycleOwner) { _, bundle ->
            if (ConfirmDialogFragment.readChoice(bundle) == DialogChoice.LEFT) {
                doLogout()
            }
        }

        parentFragmentManager.setFragmentResultListener(REQ_WITHDRAW, viewLifecycleOwner) { _, bundle ->
            if (ConfirmDialogFragment.readChoice(bundle) == DialogChoice.LEFT) {
                withdrawMember()
            }
        }

        // -----------------------------
        // Click listeners
        // -----------------------------
        binding.tvItemMyPosts.setOnClickListener { showPostTypeDialog() }

        binding.ivGoToProfile.setOnClickListener { navigateTo(ProfileFragment()) }
        binding.tvItemAlarm.setOnClickListener { navigateTo(AlarmFragment()) }
        binding.tvItemLiked.setOnClickListener { navigateTo(MyScrapFragment()) }

        binding.tvItemLogout.setOnClickListener {
            ConfirmDialogFragment
                .newInstance(REQ_LOGOUT, ConfirmDialogSpec.Logout.key)
                .show(parentFragmentManager, "confirm_logout")
        }

        // 회원탈퇴도 다이얼로그
        binding.tvItemWithdraw.setOnClickListener {
            ConfirmDialogFragment
                .newInstance(REQ_WITHDRAW, ConfirmDialogSpec.Withdraw.key)
                .show(parentFragmentManager, "confirm_withdraw")
        }

        //users에서 닉네임/학교/게시물수(sharingCount)/레벨 가져오기
        loadMyPageHeader()
    }

    private fun loadMyPageHeader() {
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = authRepository.getCurrentUserUid()
            if (uid.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요해요.", Toast.LENGTH_SHORT).show()
                goLoginClearTask()
                return@launch
            }

            userRepository.getUserProfile(uid)
                .onSuccess { user ->
                    // 1) 닉네임
                    binding.tvNickname.text = user.nickname.ifBlank {
                        getString(R.string.nickname_placeholder)
                    }

                    // 2) 대학/게시물수 (서버값: school + sharingCount)
                    val school = user.school.ifBlank { "학교" }
                    val postCount = user.sharingCount
                    binding.tvProfile.text = "${school}•게시물${postCount}"

                    // 3) 레벨바 (서버값 기반: sharingCount -> LV1~LV5)
                    val levelInt = calculateLevelFromSharingCount(postCount) // 1~5
                    val userLevel = UserLevel.from(levelInt)
                    binding.ivLevelBar.setImageResource(userLevel.levelBarRes)
                }
                .onFailure { e ->
                    Toast.makeText(
                        requireContext(),
                        "내 정보 불러오기 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    /**
     * LV1~LV5 매핑 (기획 기준 있으면 구간만 바꾸면 됨)
     */
    private fun calculateLevelFromSharingCount(sharingCount: Int): Int {
        return when {
            sharingCount >= 20 -> 5
            sharingCount >= 10 -> 4
            sharingCount >= 5 -> 3
            sharingCount >= 2 -> 2
            else -> 1
        }
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showPostTypeDialog() {
        ConfirmDialogFragment.newInstance(
            requestKey = REQ_POST_TYPE,
            specKey = ConfirmDialogSpec.PostType.key
        ).show(parentFragmentManager, "postTypeDialog")
    }

    private fun openMySharePost() {
        (activity as? MainActivity)?.openOverlay(MySharePostFragment(), "MySharePost")
    }

    private fun openMyLostFoundPost() {
        (activity as? MainActivity)?.openOverlay(MyLostFoundPostFragment(), "MyLostFoundPost")
    }

    private fun doLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            authRepository.signOut()
                .onSuccess { goLoginClearTask() }
                .onFailure { e ->
                    Toast.makeText(
                        requireContext(),
                        "로그아웃 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    private fun withdrawMember() {
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = authRepository.getCurrentUserUid()
            if (uid.isNullOrBlank()) {
                Toast.makeText(requireContext(), "로그인이 필요해요.", Toast.LENGTH_SHORT).show()
                goLoginClearTask()
                return@launch
            }

            // 1) Firestore 유저 데이터 삭제 (주의: 하위 컬렉션은 별도 삭제 필요할 수 있음)
            userRepository.deleteAllUserData(uid)
                .onFailure { e ->
                    Toast.makeText(
                        requireContext(),
                        "회원탈퇴 실패(데이터 삭제): ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

            // 2) Auth 계정 삭제
            authRepository.withdrawAccount()
                .onSuccess {
                    Toast.makeText(requireContext(), "회원탈퇴가 완료되었어요.", Toast.LENGTH_SHORT).show()
                    goLoginClearTask()
                }
                .onFailure { e ->
                    // 재인증 필요할 수 있음
                    Toast.makeText(
                        requireContext(),
                        "회원탈퇴 실패(계정 삭제): ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun goLoginClearTask() {
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
