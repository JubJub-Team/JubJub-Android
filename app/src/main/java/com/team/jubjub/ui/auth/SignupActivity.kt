package com.team.jubjub.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.team.jubjub.data.model.User
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.UserRepository
import com.team.jubjub.databinding.ActivitySignupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var userRepository: UserRepository

    private var isIdChecked = false
    private var isNicknameChecked = false
    private var isEmailChecked = false
    private var isPhoneChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
    }

    /**
     * 클릭 리스너 설정
     */
    private fun setListeners() {

        // 아이디 중복 확인
        binding.btnIdcheck.setOnClickListener { checkIdDuplicate() }

        // 비밀번호 유효성 검사
        binding.edtPw.doAfterTextChanged { validatePassword() }

        // 비밀번호 확인 검사
        binding.edtPwcheck.doAfterTextChanged { validatePasswordCheck() }

        // 닉네임 중복 확인
        binding.btnNicknamecheck.setOnClickListener { checkNicknameDuplicate() }

        // 이메일 중복 확인
        binding.btnEmailcheck.setOnClickListener { checkEmailDuplicate() }

        // 전화번호 중복 확인
        binding.btnPhonecheck.setOnClickListener { checkPhoneDuplicate() }

        // 회원가입
        binding.btnSignup.setOnClickListener { submitSignup() }

        // 취소
        binding.btnCancel.setOnClickListener { finish() }
    }

    /**
     * 아이디 중복 확인 (Firestore: users where customId == 입력값)
     */
    private fun checkIdDuplicate() {
        val id = binding.edtId.text.toString().trim()

        if (id.length < 4) {
            showMessage(binding.tvIdMsg, "*아이디는 4자 이상이어야 합니다.", false)
            isIdChecked = false
            return
        }

        lifecycleScope.launch {
            userRepository.checkCustomIdDuplicate(id)
                .onSuccess { available ->
                    if (available) {
                        showMessage(binding.tvIdMsg, "*사용 가능한 아이디입니다.", true)
                        isIdChecked = true
                    } else {
                        showMessage(binding.tvIdMsg, "*이미 사용 중인 아이디입니다.", false)
                        isIdChecked = false
                    }
                }
                .onFailure { e ->
                    showMessage(binding.tvIdMsg, "*아이디 확인 실패: ${e.message}", false)
                    isIdChecked = false
                }
        }
    }

    /**
     * 비밀번호 유효성 검사
     */
    private fun validatePassword() {
        val password = binding.edtPw.text.toString()
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")

        if (!regex.matches(password)) {
            showMessage(binding.tvPwMsg, "*영문, 숫자 조합 8~16자를 입력해 주세요.", false)
        } else {
            showMessage(binding.tvPwMsg, "*사용 가능한 비밀번호입니다.", true)
        }
    }

    /**
     * 비밀번호 확인 검사
     */
    private fun validatePasswordCheck() {
        val password = binding.edtPw.text.toString()
        val passwordCheck = binding.edtPwcheck.text.toString()

        if (passwordCheck.isNotEmpty() && password != passwordCheck) {
            showMessage(binding.tvPwcheckMsg, "*비밀번호가 일치하지 않습니다.", false)
        } else if (passwordCheck.isNotEmpty()) {
            showMessage(binding.tvPwcheckMsg, "*비밀번호가 일치합니다.", true)
        }
    }

    /**
     * 닉네임 중복 확인 (Firestore: users where nickname == 입력값)
     */
    private fun checkNicknameDuplicate() {
        val nickname = binding.edtNickname.text.toString().trim()

        if (nickname.isEmpty()) {
            showMessage(binding.tvNicknameMsg, "*올바른 닉네임을 입력해주세요.", false)
            isNicknameChecked = false
            return
        }

        lifecycleScope.launch {
            userRepository.checkNicknameDuplicate(nickname)
                .onSuccess { available ->
                    if (available) {
                        showMessage(binding.tvNicknameMsg, "*사용 가능한 닉네임입니다.", true)
                        isNicknameChecked = true
                    } else {
                        showMessage(binding.tvNicknameMsg, "*이미 사용 중인 닉네임입니다.", false)
                        isNicknameChecked = false
                    }
                }
                .onFailure { e ->
                    showMessage(binding.tvNicknameMsg, "*닉네임 확인 실패: ${e.message}", false)
                    isNicknameChecked = false
                }
        }
    }

    /**
     * 이메일 중복 확인 (Firestore 체크 + 형식 검증)
     * 참고: 실제 중복은 Auth 회원가입 시에도 잡힘.
     */
    private fun checkEmailDuplicate() {
        val email = binding.edtEmail.text.toString().trim()

        if (!email.contains("@")) {
            showMessage(binding.tvEmailMsg, "*올바른 이메일 형식이 아닙니다.", false)
            isEmailChecked = false
            return
        }

        lifecycleScope.launch {
            userRepository.checkEmailDuplicate(email)
                .onSuccess { available ->
                    if (available) {
                        showMessage(binding.tvEmailMsg, "*사용 가능한 이메일입니다.", true)
                        isEmailChecked = true
                    } else {
                        showMessage(binding.tvEmailMsg, "*이미 사용 중인 이메일입니다.", false)
                        isEmailChecked = false
                    }
                }
                .onFailure { e ->
                    showMessage(binding.tvEmailMsg, "*이메일 확인 실패: ${e.message}", false)
                    isEmailChecked = false
                }
        }
    }

    /**
     * 전화번호 중복 확인 (Firestore: users where phone == 입력값)
     */
    private fun checkPhoneDuplicate() {
        val phone = binding.edtPhone.text.toString().trim()

        if (phone.length < 10) {
            showMessage(binding.tvPhoneMsg, "*올바른 전화번호를 입력해 주세요.", false)
            isPhoneChecked = false
            return
        }

        lifecycleScope.launch {
            userRepository.checkPhoneDuplicate(phone)
                .onSuccess { available ->
                    if (available) {
                        showMessage(binding.tvPhoneMsg, "*사용 가능한 전화번호입니다.", true)
                        isPhoneChecked = true
                    } else {
                        showMessage(binding.tvPhoneMsg, "*이미 사용 중인 전화번호입니다.", false)
                        isPhoneChecked = false
                    }
                }
                .onFailure { e ->
                    showMessage(binding.tvPhoneMsg, "*전화번호 확인 실패: ${e.message}", false)
                    isPhoneChecked = false
                }
        }
    }

    /**
     * 회원가입 처리
     * 1) Auth 가입 (email, password) -> uid
     * 2) Firestore users/{uid} 저장 (UserRepository.saveUserProfile)
     */
    private fun submitSignup() {
        if (!isIdChecked || !isNicknameChecked || !isEmailChecked || !isPhoneChecked) {
            showToast("중복 확인을 모두 완료해 주세요.")
            return
        }

        val customId = binding.edtId.text.toString().trim()
        val nickname = binding.edtNickname.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val phone = binding.edtPhone.text.toString().trim()
        val password = binding.edtPw.text.toString().trim()
        val passwordCheck = binding.edtPwcheck.text.toString().trim()

        if (password.isBlank() || passwordCheck.isBlank()) {
            showToast("비밀번호를 입력해 주세요.")
            return
        }
        if (password != passwordCheck) {
            showToast("비밀번호가 일치하지 않습니다.")
            return
        }
        if (!email.contains("@")) {
            showToast("이메일 형식을 확인해 주세요.")
            return
        }

        binding.btnSignup.isEnabled = false

        lifecycleScope.launch {
            // 1) Auth 회원가입
            authRepository.signUp(email, password)
                .onSuccess { uid ->
                    // 2) Firestore 프로필 저장
                    val user = User(
                        userId = uid,
                        customId = customId,
                        nickname = nickname,
                        email = email,
                        phone = phone
                    )

                    userRepository.saveUserProfile(user)
                        .onSuccess {
                            showToast("회원가입 완료")
                            finish()
                        }
                        .onFailure { e ->
                            binding.btnSignup.isEnabled = true
                            android.util.Log.e("Signup", "saveUserProfile failed", e)
                            showToast("프로필 저장 실패: ${e.localizedMessage ?: e.javaClass.simpleName}")
                        }
                }
                .onFailure { e ->
                    binding.btnSignup.isEnabled = true
                    showToast("회원가입 실패: ${e.message}")
                }
        }
    }

    /**
     * 안내 메시지 출력
     */
    private fun showMessage(
        textView: TextView,
        message: String,
        isSuccess: Boolean
    ) {
        textView.text = message
        textView.setTextColor(
            if (isSuccess) Color.parseColor("#20CD7C")
            else Color.parseColor("#E53935")
        )
        textView.visibility = View.VISIBLE
    }

    /**
     * 토스트 출력
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
