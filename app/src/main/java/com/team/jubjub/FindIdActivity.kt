package com.team.jubjub

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.team.jubjub.databinding.ActivityFindIdBinding

class FindIdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindIdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFindIdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* ------------------------
           비밀번호 찾기 이동
        ------------------------ */
        binding.tvFindPwAction.setOnClickListener {
            startActivity(Intent(this, FindPwActivity::class.java))
        }
    }
}
