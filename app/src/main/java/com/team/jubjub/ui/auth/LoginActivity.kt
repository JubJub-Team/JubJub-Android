package com.team.jubjub.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.team.jubjub.MainActivity
import com.team.jubjub.ui.auth.SignupActivity
import com.team.jubjub.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            val id = binding.edtId.text.toString().trim()
            val pw = binding.edtPw.text.toString().trim()

            // 에러 초기화
            binding.tilId.error = null
            binding.tilPassword.error = null

            when {
                id.isEmpty() -> {
                    binding.tilId.error = "아이디를 입력해주세요."
                    binding.edtId.requestFocus()
                    return@setOnClickListener
                }

                pw.isEmpty() -> {
                    binding.tilPassword.error = "비밀번호를 입력해주세요."
                    binding.edtPw.requestFocus()
                    return@setOnClickListener
                }
            }

            // TODO: 로그인 API 연동 예정
            goToHome()
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}