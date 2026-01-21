package com.team.jubjub

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.team.jubjub.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val fragments = mutableMapOf<Int, Fragment>()
    private var selectedId: Int = -1  //초기 선택 안함

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 커스텀 하단바 버튼 클릭 연결
        binding.navHome.setOnClickListener { switchTab(R.id.nav_home) }
        binding.navShare.setOnClickListener { switchTab(R.id.nav_share) }
        binding.navLostFound.setOnClickListener { switchTab(R.id.nav_lost_found) }
        binding.navWrite.setOnClickListener { switchTab(R.id.nav_write) }
        binding.navProfile.setOnClickListener { switchTab(R.id.nav_profile) }

        // 첫 화면
        if (savedInstanceState == null) {
            switchTab(R.id.nav_home)
        }

        //상단 인셋 처리
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.fragmentContainer.setPadding(sys.left, sys.top, sys.right, 0)
            insets
        }
    }

    private fun switchTab(itemId: Int) {
        if (selectedId == itemId) return

        val fm = supportFragmentManager
        val tx = fm.beginTransaction()

        // 이전 프래그먼트 숨김
        fragments[selectedId]?.let { tx.hide(it) }

        // 새 프래그먼트 준비
        val tag = itemId.toString()
        val target = fragments[itemId] ?: fm.findFragmentByTag(tag) ?: when (itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_share -> ShareFragment()
            R.id.nav_lost_found -> LostFoundFragment()
            R.id.nav_write -> WriteFragment()
            R.id.nav_profile -> ProfileFragment()
            else -> HomeFragment()
        }.also {
            fragments[itemId] = it
            tx.add(R.id.fragmentContainer, it, tag)
        }

        tx.show(target)
        tx.commit()

        selectedId = itemId
        updateSelectedUi(itemId)
    }

    private fun updateSelectedUi(selected: Int) {
        val main = ContextCompat.getColor(this, R.color.main)
        val gray = ContextCompat.getColor(this, android.R.color.darker_gray)

        val buttons = listOf(
            binding.navHome,
            binding.navShare,
            binding.navLostFound,
            binding.navWrite,
            binding.navProfile
        )

        buttons.forEach { btn ->
            val color = if (btn.id == selected) main else gray
            btn.imageTintList = ColorStateList.valueOf(color)
        }
    }
}
