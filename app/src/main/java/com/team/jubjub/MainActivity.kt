package com.team.jubjub

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.team.jubjub.databinding.ActivityMainBinding
import com.team.jubjub.ui.home.HomeFragment
import com.team.jubjub.ui.lostfound.LostFoundFragment
import com.team.jubjub.ui.mypage.MyPageFragment
import com.team.jubjub.ui.share.ShareFragment
import com.team.jubjub.ui.write.WriteFragment
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragments = mutableMapOf<Int, Fragment>()
    private var selectedId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navHome.setOnClickListener { switchTab(R.id.nav_home) }
        binding.navShare.setOnClickListener { switchTab(R.id.nav_share) }
        binding.navLostFound.setOnClickListener { switchTab(R.id.nav_lost_found) }
        binding.navWrite.setOnClickListener { switchTab(R.id.nav_write) }
        binding.navMyPage.setOnClickListener { switchTab(R.id.nav_my_page) }

        if (savedInstanceState == null) {
            switchTab(R.id.nav_home)
        }

        applyEdgeToEdgeInsets()
    }

    private fun applyEdgeToEdgeInsets() {
        val baseMainLeft = binding.main.paddingLeft
        val baseMainRight = binding.main.paddingRight
        val baseMainTop = binding.main.paddingTop

        val baseBottomMargin =
            (binding.bottomBarCard.layoutParams as ConstraintLayout.LayoutParams).bottomMargin

        val baseBottomBarPaddingBottom = binding.bottomBarCard.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val mandatoryGestureBottom =
                insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures()).bottom

            // 전체 레이아웃: 상단/좌우만 반영
            binding.main.updatePadding(
                left = baseMainLeft + sysBars.left,
                right = baseMainRight + sysBars.right,
                top = baseMainTop + sysBars.top
            )

            val isThreeButton =
                navBottom > dpToPx(12) && mandatoryGestureBottom <= dpToPx(4)

            binding.bottomBarCard.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = if (isThreeButton) 0 else baseBottomMargin
            }

            binding.bottomBarCard.updatePadding(
                bottom = if (isThreeButton) baseBottomBarPaddingBottom + navBottom else baseBottomBarPaddingBottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(binding.main)
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private fun switchTab(itemId: Int) {
        if (selectedId == itemId) return

        supportFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )


        val fm = supportFragmentManager
        val tx = fm.beginTransaction()

        fragments[selectedId]?.let { tx.hide(it) }

        val tag = itemId.toString()
        val target = fragments[itemId] ?: fm.findFragmentByTag(tag) ?: when (itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_share -> ShareFragment()
            R.id.nav_lost_found -> LostFoundFragment()
            R.id.nav_write -> WriteFragment()
            R.id.nav_my_page -> MyPageFragment()
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
            binding.navMyPage
        )

        buttons.forEach { btn ->
            val color = if (btn.id == selected) main else gray
            btn.imageTintList = ColorStateList.valueOf(color)
        }
    }
}
