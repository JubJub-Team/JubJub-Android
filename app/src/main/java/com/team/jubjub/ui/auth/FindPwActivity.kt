package com.team.jubjub.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.team.jubjub.databinding.ActivityFindPwBinding

class FindPwActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindPwBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFindPwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* ------------------------
           아이디 찾기 이동
        ------------------------ */
        binding.tvFindIdAction.setOnClickListener {
            startActivity(Intent(this, FindIdActivity::class.java))
        }
    }
}