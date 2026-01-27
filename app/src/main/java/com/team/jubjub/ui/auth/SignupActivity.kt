package com.team.jubjub.ui.auth

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.databinding.ActivitySignupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    @Inject lateinit var authRepository: AuthRepository

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    private var selectedProfileImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedProfileImageUri = uri
                // 갱신 보장용
                binding.icProfile.setImageURI(null)
                binding.icProfile.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
    }

    private fun setListeners() {
        // 프로필 사진 선택(선택사항)
        binding.imgEllipse.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.icProfile.setOnClickListener { pickImageLauncher.launch("image/*") }

        // 비밀번호 유효성(원하면 메시지 TextView 연결해서 표시 가능)
        binding.edtPw.doAfterTextChanged { /* validatePassword() */ }
        binding.edtPwcheck.doAfterTextChanged { /* validatePasswordCheck() */ }

        // 회원가입/취소
        binding.btnSignup.setOnClickListener { submitSignup() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun submitSignup() {
        val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        val password = binding.edtPw.text?.toString()?.trim().orEmpty()
        val passwordCheck = binding.edtPwcheck.text?.toString()?.trim().orEmpty()
        val nickname = binding.edtNickname.text?.toString()?.trim().orEmpty()
        val school = binding.edtSchool.text?.toString()?.trim().orEmpty()

        // 1) 입력 검증
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("이메일 형식을 확인해 주세요.")
            return
        }
        if (password.length !in 8..16) {
            showToast("비밀번호는 8~16자로 입력해 주세요.")
            return
        }
        if (password != passwordCheck) {
            showToast("비밀번호가 일치하지 않습니다.")
            return
        }
        if (nickname.isBlank()) {
            showToast("닉네임을 입력해 주세요.")
            return
        }
        if (school.isBlank()) {
            showToast("학교를 입력해 주세요.")
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            // 2) Firebase Auth 회원가입
            authRepository.signUp(email, password)
                .onSuccess { uid ->

                    // 3) 이메일 인증 메일 발송
                    try {
                        auth.currentUser?.sendEmailVerification()?.await()
                    } catch (e: Exception) {
                        setLoading(false)
                        showToast("인증메일 전송 실패: ${e.message}")
                        return@launch
                    }

                    // 4) (선택) 프로필 이미지 업로드
                    val profileUrl = try {
                        uploadProfileImageIfNeeded(uid)
                    } catch (e: Exception) {
                        ""
                    }

                    // 5) Firestore에 유저 프로필 저장
                    //    (닉네임/학교는 Auth에 기본 필드가 아니라 Firestore 등에 저장해야 함)
                    val profile = hashMapOf(
                        "userId" to uid,
                        "email" to email,
                        "nickname" to nickname,
                        "school" to school,
                        "profileImageUrl" to profileUrl,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    try {
                        db.collection("users").document(uid).set(profile).await()
                        setLoading(false)
                        showToast("가입 완료! 이메일 인증 후 로그인해 주세요.")
                        finish()
                    } catch (e: Exception) {
                        setLoading(false)
                        showToast("프로필 저장 실패: ${e.message}")
                    }
                }
                .onFailure { e ->
                    setLoading(false)
                    showToast("회원가입 실패: ${e.message}")
                }
        }
    }

    private suspend fun uploadProfileImageIfNeeded(uid: String): String {
        val uri = selectedProfileImageUri ?: return ""
        val ref = storage.reference.child("users/$uid/profile.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnSignup.isEnabled = !isLoading
        binding.btnCancel.isEnabled = !isLoading

        // 로딩뷰 있으면 여기서 제어(없으면 무시해도 됨)
        // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
