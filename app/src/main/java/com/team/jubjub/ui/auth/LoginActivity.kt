package com.team.jubjub.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.team.jubjub.MainActivity
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 이미 로그인 상태면 바로 홈
        authRepository.getCurrentUserUid()?.let {
            goToHome()
            return
        }

        /* ------------------------
           아이디 찾기 이동
        ------------------------ */
        binding.tvFindId.setOnClickListener {
            startActivity(Intent(this, FindIdActivity::class.java))
        }

        /* ------------------------
           비밀번호 찾기 이동
        ------------------------ */
        binding.tvFindPw.setOnClickListener {
            startActivity(Intent(this, FindPwActivity::class.java))
        }

        /* ------------------------
           회원가입 이동
        ------------------------ */
        binding.tvSignupAction.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        /* ------------------------
           로그인 버튼
        ------------------------ */
        binding.btnLogin.setOnClickListener {
            val email = binding.edtId.text.toString().trim()
            val pw = binding.edtPw.text.toString().trim()

            binding.tilId.error = null
            binding.tilPassword.error = null

            when {
                email.isEmpty() -> {
                    binding.tilId.error = "이메일을 입력해주세요."
                    binding.edtId.requestFocus()
                    return@setOnClickListener
                }
                pw.isEmpty() -> {
                    binding.tilPassword.error = "비밀번호를 입력해주세요."
                    binding.edtPw.requestFocus()
                    return@setOnClickListener
                }
            }

            // 버튼 중복 클릭 방지(선택)
            binding.btnLogin.isEnabled = false

            lifecycleScope.launch {
                authRepository.signIn(email, pw)
                    .onSuccess { uid ->
                        // uid 확보 성공
                        Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                        goToHome()
                    }
                    .onFailure { e ->
                        binding.btnLogin.isEnabled = true

                        val rawMessage = e.message ?: ""
                        val friendlyMessage = when {
                            rawMessage.contains("badly formatted") -> "이메일 형식이 올바르지 않습니다."
                            rawMessage.contains("invalid user") || rawMessage.contains("user not found") -> "등록되지 않은 사용자입니다."
                            rawMessage.contains("wrong password") -> "비밀번호가 틀렸습니다."
                            else -> "로그인 실패: 정보를 다시 확인해주세요."
                        }

                        binding.tilPassword.error = friendlyMessage
                    }
            }
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}