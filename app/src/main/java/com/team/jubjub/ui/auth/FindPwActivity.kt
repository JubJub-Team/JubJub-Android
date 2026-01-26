package com.team.jubjub.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.data.repository.AuthRepositoryImpl
import com.team.jubjub.databinding.ActivityFindPwBinding
import kotlinx.coroutines.launch

class FindPwActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindPwBinding

    private val authRepository: AuthRepository = AuthRepositoryImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFindPwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 아이디 찾기 화면 이동
        binding.tvFindIdAction.setOnClickListener {
            startActivity(Intent(this, FindIdActivity::class.java))
        }

        // 비밀번호 찾기 버튼
        binding.btnFindPw.setOnClickListener {
            val id = binding.edtId.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()

            if (id.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "아이디와 이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendPasswordReset(email)
        }
    }

    private fun sendPasswordReset(email: String) {
        lifecycleScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)

            result.onSuccess {
                Toast.makeText(
                    this@FindPwActivity,
                    "비밀번호 재설정 이메일을 전송했습니다",
                    Toast.LENGTH_LONG
                ).show()
            }.onFailure {
                Toast.makeText(
                    this@FindPwActivity,
                    "이메일 전송 실패: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}