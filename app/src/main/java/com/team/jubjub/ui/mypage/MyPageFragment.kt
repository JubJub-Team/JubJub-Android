package com.team.jubjub.ui.mypage

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.team.jubjub.ConfirmDialogFragment
import com.team.jubjub.ConfirmDialogSpec
import com.team.jubjub.DialogChoice
import com.team.jubjub.MainActivity
import com.team.jubjub.R
import com.team.jubjub.data.model.enums.UserLevel
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.UserRepository
import com.team.jubjub.databinding.FragmentMyPageBinding
import com.team.jubjub.ui.auth.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class MyPageFragment : Fragment() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var userRepository: UserRepository

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val REQ_MY_POST_SELECT = "req_my_post_select"
        private const val REQ_LOGOUT = "req_logout"
        private const val REQ_WITHDRAW = "req_withdraw"
        private const val TAG = "MyPageFragment"
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

        // ✅ 프사 연결
        loadMyProfileImage()

        // TODO: 서버/로컬에서 받아온 레벨 값으로 교체
        val levelInt = 2
        val userLevel = UserLevel.from(levelInt)
        binding.ivLevelBar.setImageResource(userLevel.levelBarRes)

        // -----------------------------
        // 내 게시물 종류 선택 다이얼로그 결과
        // -----------------------------
        parentFragmentManager.setFragmentResultListener(REQ_MY_POST_SELECT, viewLifecycleOwner) { _, bundle ->
            val choice = ConfirmDialogFragment.readChoice(bundle)
            when (choice) {
                DialogChoice.LEFT -> openMyLostFoundPost()
                DialogChoice.RIGHT -> openMySharePost()
            }
        }

        binding.tvItemMyPosts.setOnClickListener { showPostTypeDialog() }

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
        // 로그아웃
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
        // 회원탈퇴
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

    /**
     * ✅ users/{uid}.profileImageUrl -> Bitmap으로 로드 -> iv_profile에 표시
     * - Glide/Coil 없이 URL을 직접 열어서 Bitmap 디코딩
     * - profileImageUrl은 https://... 형태여야 함 (gs://면 이 방식으로 불가)
     */
    private fun loadMyProfileImage() {
        // 먼저 기본 이미지
        binding.ivProfile.setImageResource(R.drawable.ic_profile)

        val uid = authRepository.getCurrentUserUid()
        if (uid.isNullOrBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            userRepository.getUserProfile(uid)
                .onSuccess { user ->
                    val url = user.profileImageUrl.trim()
                    Log.d(TAG, "profileImageUrl=$url")

                    if (url.isBlank()) return@onSuccess

                    if (url.startsWith("gs://")) {
                        // gs://는 URL.openStream으로 못 열어 (downloadUrl https 저장 필요)
                        Log.w(TAG, "profileImageUrl is gs://. https downloadUrl로 저장되어야 표시 가능.")
                        return@onSuccess
                    }

                    val bmp = withContext(Dispatchers.IO) { urlToBitmap(url) }
                    if (!isAdded) return@launch

                    if (bmp != null) {
                        binding.ivProfile.setImageBitmap(bmp)
                    } else {
                        binding.ivProfile.setImageResource(R.drawable.ic_profile)
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "getUserProfile failed: ${e.message}", e)
                    binding.ivProfile.setImageResource(R.drawable.ic_profile)
                }
        }
    }

    private fun urlToBitmap(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                doInput = true
            }
            conn.connect()

            if (conn.responseCode !in 200..299) {
                Log.w(TAG, "HTTP ${conn.responseCode} for $urlString")
                conn.disconnect()
                return null
            }

            conn.inputStream.use { stream ->
                BitmapFactory.decodeStream(stream)
            }.also {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "urlToBitmap error: ${e.message}", e)
            null
        }
    }

    private fun doLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            authRepository.signOut()
                .onSuccess { goLoginClearTask() }
                .onFailure { e ->
                    Toast.makeText(requireContext(), "로그아웃 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * 회원탈퇴 연결: Firestore(users) 삭제 + FirebaseAuth 계정 삭제
     *
     * 순서가 중요:
     * 1) Firestore 데이터 삭제 (userRepository.deleteAllUserData)
     * 2) Auth 계정 삭제 (authRepository.withdrawAccount)
     *
     *  Auth 삭제는 "최근 로그인" 요구할 수 있음.
     * 그 경우 withdrawAccount()에서 실패가 떨어지고 메시지가 나올 것.
     */
    private fun withdrawMember() {
        val uid = authRepository.getCurrentUserUid()
        if (uid.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 1) DB 삭제
            userRepository.deleteAllUserData(uid)
                .onFailure { e ->
                    Toast.makeText(requireContext(), "회원 데이터 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

            // 2) Auth 계정 삭제
            authRepository.withdrawAccount()
                .onFailure { e ->
                    Toast.makeText(requireContext(), "계정 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

            Toast.makeText(requireContext(), "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            goLoginClearTask()
        }
    }

    private fun goLoginClearTask() {
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun openMySharePost() {
        (activity as? MainActivity)?.openOverlay(MySharePostFragment(), "MySharePost")
    }

    private fun openMyLostFoundPost() {
        (activity as? MainActivity)?.openOverlay(MyLostFoundPostFragment(), "MyLostFoundPost")
    }

    private fun showPostTypeDialog() {
        ConfirmDialogFragment.newInstance(
            requestKey = REQ_MY_POST_SELECT,
            specKey = ConfirmDialogSpec.PostType.key
        ).show(parentFragmentManager, "postTypeDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        parentFragmentManager.clearFragmentResultListener(REQ_MY_POST_SELECT)
        parentFragmentManager.clearFragmentResultListener(REQ_LOGOUT)
        parentFragmentManager.clearFragmentResultListener(REQ_WITHDRAW)
        _binding = null
    }
}