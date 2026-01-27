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
                import com.team.jubjub.ui.write.WriteLostFoundFragment
                import com.team.jubjub.ui.write.WriteShareFragment
                import dagger.hilt.android.AndroidEntryPoint

                @AndroidEntryPoint
                class MainActivity : AppCompatActivity() {

                    companion object {
                        private const val REQ_POST_TYPE = "req_post_type"
                    }

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

                        // write는 탭 전환이 아니라 다이얼로그 -> overlay로 이동
                        binding.navWrite.setOnClickListener { showPostTypeDialog() }

                        binding.navMyPage.setOnClickListener { switchTab(R.id.nav_my_page) }

                        // 다이얼로그 결과 리스너
                        setupPostTypeDialogResult()

                        supportFragmentManager.addOnBackStackChangedListener {
                            if (supportFragmentManager.backStackEntryCount == 0) {
                                fragments[selectedId]?.let { current ->
                                    supportFragmentManager.beginTransaction()
                                        .show(current)
                                        .commit()
                                }
                            }
                            // 오버레이 열림/닫힘 포함해서 항상 아이콘 갱신
                            updateSelectedUi(selectedId)
                        }

                        if (savedInstanceState == null) {
                            switchTab(R.id.nav_home)
                        } else {
                            // 복원 시에도 UI 맞추기
                            updateSelectedUi(selectedId)
                        }

                        applyEdgeToEdgeInsets()
                    }

                    // 작성 타입 선택 다이얼로그
                    private fun showPostTypeDialog() {
                        ConfirmDialogFragment.newInstance(
                            requestKey = REQ_POST_TYPE,
                            specKey = ConfirmDialogSpec.WriteStatus.key
                        ).show(supportFragmentManager, "WriteStatusDialog")
                    }

                    // 다이얼로그 결과 -> MyPageFragment처럼 openOverlay로 띄우기
                    private fun setupPostTypeDialogResult() {
                        supportFragmentManager.setFragmentResultListener(REQ_POST_TYPE, this) { _, bundle ->
                            val target: Fragment = when (ConfirmDialogFragment.readChoice(bundle)) {
                                DialogChoice.LEFT -> WriteLostFoundFragment()
                                DialogChoice.RIGHT -> WriteShareFragment()
                            }
                            openOverlay(target, tag = target::class.java.name)
                        }
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

                        val fm = supportFragmentManager

                        // 오버레이(BackStack) 제거 로직 (기존 유지)
                        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        fm.executePendingTransactions()

                        val tx = fm.beginTransaction()

                        // 현재 탭 숨기기
                        fragments[selectedId]?.let { tx.hide(it) }

                        val tag = itemId.toString()
                        var target = fragments[itemId]

                        if (target == null) {
                            target = when (itemId) {
                                R.id.nav_home -> HomeFragment()
                                R.id.nav_share -> ShareFragment()
                                R.id.nav_lost_found -> LostFoundFragment()
                                R.id.nav_my_page -> MyPageFragment()
                                // nav_write는 탭이 아니므로 여기서 처리하지 않음
                                else -> HomeFragment()
                            }
                            fragments[itemId] = target
                            tx.add(R.id.fragmentContainer, target, tag)
                        } else {
                            tx.show(target)
                        }

                        tx.commit()

                        selectedId = itemId
                        updateSelectedUi(itemId)
                    }

                    fun selectTab(itemId: Int) {
        switchTab(itemId)
    }

    fun openOverlay(fragment: Fragment, tag: String = fragment::class.java.name) {
        val fm = supportFragmentManager
        val tx = fm.beginTransaction()

        fragments[selectedId]?.let { tx.hide(it) }

        tx.add(R.id.fragmentContainer, fragment, tag)
            .addToBackStack(tag)
            .commit()

        // 오버레이가 최상단이 되었을 때 write 아이콘이 초록이 되도록 즉시 갱신 시도
        // (commit 타이밍 이슈 방지 위해 pending 처리)
        fm.executePendingTransactions()
        updateSelectedUi(selectedId)
    }

    private fun updateSelectedUi(selected: Int) {
        val main = ContextCompat.getColor(this, R.color.main)
        val gray = ContextCompat.getColor(this, android.R.color.darker_gray)

        val currentTop = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        val isWriteScreen = currentTop is WriteShareFragment || currentTop is WriteLostFoundFragment

        val buttons = listOf(
            binding.navHome,
            binding.navShare,
            binding.navLostFound,
            binding.navWrite,
            binding.navMyPage
        )

        buttons.forEach { btn ->
            val color = if (isWriteScreen) {
                // Write 화면이면 4번만 초록, 나머지 회색
                if (btn.id == R.id.nav_write) main else gray
            } else {
                // 일반 탭 화면이면 selected 탭만 초록
                if (btn.id == selected) main else gray
            }

            btn.imageTintList = ColorStateList.valueOf(color)
        }
    }
}

