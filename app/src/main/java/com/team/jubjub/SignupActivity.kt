package com.team.jubjub

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.team.jubjub.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

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
        binding.btnIdcheck.setOnClickListener {
            checkIdDuplicate()
        }

        // 비밀번호 유효성 검사
        binding.edtPw.doAfterTextChanged {
            validatePassword()
        }

        // 비밀번호 확인 검사
        binding.edtPwcheck.doAfterTextChanged {
            validatePasswordCheck()
        }

        // 닉네임 중복 확인
        binding.btnNicknamecheck.setOnClickListener {
            checkNicknameDuplicate()
        }

        // 이메일 중복 확인
        binding.btnEmailcheck.setOnClickListener {
            checkEmailDuplicate()
        }

        // 전화번호 중복 확인
        binding.btnPhonecheck.setOnClickListener {
            checkPhoneDuplicate()
        }

        // 회원가입
        binding.btnSignup.setOnClickListener {
            submitSignup()
        }

        // 취소
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    /**
     * 아이디 중복 확인
     */
    private fun checkIdDuplicate() {
        val id = binding.edtId.text.toString()

        if (id.length < 4) {
            showMessage(binding.tvIdMsg, "*아이디는 4자 이상이어야 합니다.", false)
            isIdChecked = false
            return
        }

        // TODO: 아이디 중복 확인 API
        if (id == "admin" || id == "test") {
            showMessage(binding.tvIdMsg, "*이미 사용 중인 아이디입니다.", false)
            isIdChecked = false
        } else {
            showMessage(binding.tvIdMsg, "*사용 가능한 아이디입니다.", true)
            isIdChecked = true
        }
    }

    /**
     * 비밀번호 유효성 검사
     */
    private fun validatePassword() {
        val password = binding.edtPw.text.toString()
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$")

        if (!regex.matches(password)) {
            showMessage(
                binding.tvPwMsg,
                "*영문, 숫자 조합 8~16자를 입력해 주세요.",
                false
            )
        } else {
            showMessage(
                binding.tvPwMsg,
                "*사용 가능한 비밀번호입니다.",
                true
            )
        }
    }

    /**
     * 비밀번호 확인 검사
     */
    private fun validatePasswordCheck() {
        val password = binding.edtPw.text.toString()
        val passwordCheck = binding.edtPwcheck.text.toString()

        if (passwordCheck.isNotEmpty() && password != passwordCheck) {
            showMessage(
                binding.tvPwcheckMsg,
                "*비밀번호가 일치하지 않습니다.",
                false
            )
        } else if (passwordCheck.isNotEmpty()) {
            showMessage(
                binding.tvPwcheckMsg,
                "*비밀번호가 일치합니다.",
                true
            )
        }
    }

    /**
     * 닉네임 중복 확인
     */
    private fun checkNicknameDuplicate() {
        val nickname = binding.edtNickname.text.toString()

        // TODO: 닉네임 중복 확인 API
        if (nickname == "관리자") {
            showMessage(binding.tvNicknameMsg, "*이미 사용 중인 닉네임입니다.", false)
            isNicknameChecked = false
        } else {
            showMessage(binding.tvNicknameMsg, "*사용 가능한 닉네임입니다.", true)
            isNicknameChecked = true
        }
    }

    /**
     * 이메일 중복 확인
     */
    private fun checkEmailDuplicate() {
        val email = binding.edtEmail.text.toString()

        // TODO: 이메일 중복 확인 API
        if (!email.contains("@")) {
            showMessage(binding.tvEmailMsg, "*올바른 이메일 형식이 아닙니다.", false)
            isEmailChecked = false
        } else {
            showMessage(binding.tvEmailMsg, "*사용 가능한 이메일입니다.", true)
            isEmailChecked = true
        }
    }

    /**
     * 전화번호 중복 확인
     */
    private fun checkPhoneDuplicate() {
        val phone = binding.edtPhone.text.toString()

        // TODO: 전화번호 중복 확인 API
        if (phone.length < 10) {
            showMessage(binding.tvPhoneMsg, "*올바른 전화번호를 입력해 주세요.", false)
            isPhoneChecked = false
        } else {
            showMessage(binding.tvPhoneMsg, "*사용 가능한 전화번호입니다.", true)
            isPhoneChecked = true
        }
    }

    /**
     * 회원가입 처리
     */
    private fun submitSignup() {
        if (!isIdChecked || !isNicknameChecked || !isEmailChecked || !isPhoneChecked) {
            showToast("중복 확인을 모두 완료해 주세요.")
            return
        }

        // TODO: 회원가입 API
        showToast("회원가입 완료 (임시)")
        finish()
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