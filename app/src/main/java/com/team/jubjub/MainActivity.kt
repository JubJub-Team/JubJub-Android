package com.team.jubjub

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.team.jubjub.databinding.ActivityMainBinding
import com.team.jubjub.ui.home.HomeFragment
import com.team.jubjub.ui.lostfound.LostFoundFragment
import com.team.jubjub.ui.mypage.ProfileFragment
import com.team.jubjub.ui.share.ShareFragment
import com.team.jubjub.ui.write.WriteFragment
import androidx.core.view.updateLayoutParams
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val fragments = mutableMapOf<Int, Fragment>()
    private var selectedId: Int = -1  // 초기 선택 안함

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

        // 인셋 처리: 상단(status bar) + 하단(navigation bar)
        applyEdgeToEdgeInsets()
    }

    private fun applyEdgeToEdgeInsets() {
        // 기존 패딩
        val baseMainPaddingLeft = binding.main.paddingLeft
        val baseMainPaddingRight = binding.main.paddingRight

        // bottom_bar_card의 기본marginBottom 저장
        val baseBottomMargin =
            (binding.bottomBarCard.layoutParams as ConstraintLayout.LayoutParams).bottomMargin

        val extraLiftPx = 50 // 올림 픽셀

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // 메인 좌우 시스템바 패딩
            binding.main.updatePadding(
                left = baseMainPaddingLeft + sysBars.left,
                right = baseMainPaddingRight + sysBars.right
            )

            // 시스템 하단바가 있는 경우 커스텀 하단바 위로 올림
            val newBottomMargin = if (navBars.bottom > 0) {
                baseBottomMargin + extraLiftPx
            } else {
                baseBottomMargin
            }

            binding.bottomBarCard.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = newBottomMargin
            }

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
