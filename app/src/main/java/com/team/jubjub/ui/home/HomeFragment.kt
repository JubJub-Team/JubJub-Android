package com.team.jubjub.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.team.jubjub.MainActivity
import com.team.jubjub.R
import com.team.jubjub.data.model.Post
import com.team.jubjub.databinding.FragmentHomeBinding
import com.team.jubjub.ui.mypage.AlarmFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupSearch()
        setupAlarmButton()
        setupProfileButton()
        setupMoreButtons()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectUiState()
        viewModel.loadHome()
    }

    private fun collectUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    bindSharing(state.sharingTop2) // 나눔 최신 2개
                    bindLost(state.lostTop2)       // 분실 최신 2개

                    // 로딩/에러 UI 원하면 여기서 처리
                    // 예)
                    // binding.progressBar.isVisible = state.isLoading
                    // state.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    // -------------------------
    // 버튼 / 검색
    // -------------------------

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }

        binding.ivSearch.setOnClickListener {
            performSearch()
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isEmpty()) return
        // TODO: 검색 처리
    }

    private fun setupAlarmButton() {
        binding.ibAlarm.setOnClickListener {
            (requireActivity() as MainActivity).openOverlay(AlarmFragment())
        }
    }

    private fun setupProfileButton() {
        binding.ibProfile.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_my_page)
        }
    }

    private fun setupMoreButtons() {
        binding.ibMoreShare.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_share)
        }

        binding.ibMoreLostFound.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_lost_found)
        }
    }

    private fun setupAlarmButton() {
        binding.ibAlarm.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AlarmFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupProfileButton() {
        binding.ibProfile.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_my_page)
        }
    }

    private fun setupMoreButtons() {
        binding.ibMoreShare.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_share)
        }

        binding.ibMoreLostFound.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(R.id.nav_lost_found)
        }
    }

    // -------------------------
    // 홈 카드 데이터 바인딩
    // -------------------------

    private fun bindSharing(list: List<Post>) {
        val first = list.getOrNull(0)
        val second = list.getOrNull(1)

        // 데이터 없으면 카드 숨김
        binding.cvFirstBoard.visibility = if (first == null) View.GONE else View.VISIBLE
        binding.cvSecondBoard.visibility = if (second == null) View.GONE else View.VISIBLE

        // 1번 카드
        binding.tvFirstBoardUserName.text = first?.writerNickname.orEmpty()
        binding.tvFirstBoardTitle.text = first?.title.orEmpty()
        binding.tvFirstBoardBodyText.text = first?.content.orEmpty()
        binding.tvFirstBoardTime.text = first?.createdAt?.toDate()?.let { formatDate(it) }.orEmpty()

        // 2번 카드
        binding.tvSecondBoardUserName.text = second?.writerNickname.orEmpty()
        binding.tvSecondBoardTitle.text = second?.title.orEmpty()
        binding.tvSecondBoardBodyText.text = second?.content.orEmpty()
        binding.tvSecondBoardTime.text = second?.createdAt?.toDate()?.let { formatDate(it) }.orEmpty()

        // 카드 클릭 -> 상세로 이동
        binding.cvFirstBoard.setOnClickListener { first?.let { openDetail(it.postId) } }
        binding.cvSecondBoard.setOnClickListener { second?.let { openDetail(it.postId) } }
    }

    private fun bindLost(list: List<Post>) {
        val third = list.getOrNull(0)
        val fourth = list.getOrNull(1)

        binding.cvThirdBoard.visibility = if (third == null) View.GONE else View.VISIBLE
        binding.cvFourthBoard.visibility = if (fourth == null) View.GONE else View.VISIBLE

        // 3번 카드
        binding.tvThirdBoardUserName.text = third?.writerNickname.orEmpty()
        binding.tvThirdBoardTitle.text = third?.title.orEmpty()
        binding.tvThirdBoardBodyText.text = third?.content.orEmpty()
        binding.tvThirdBoardTime.text = third?.createdAt?.toDate()?.let { formatDate(it) }.orEmpty()

        // 4번 카드
        binding.tvFourthBoardUserName.text = fourth?.writerNickname.orEmpty()
        binding.tvFourthBoardTitle.text = fourth?.title.orEmpty()
        binding.tvFourthBoardBodyText.text = fourth?.content.orEmpty()
        binding.tvFourthBoardTime.text = fourth?.createdAt?.toDate()?.let { formatDate(it) }.orEmpty()

        binding.cvThirdBoard.setOnClickListener { third?.let { openDetail(it.postId) } }
        binding.cvFourthBoard.setOnClickListener { fourth?.let { openDetail(it.postId) } }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("MM/dd  HH:mm", Locale.KOREA).format(date)
    }

    private fun openDetail(postId: String) {
        // TODO: 상세 화면 이동 구현 (Navigation 사용이면 navigate, 아니면 transaction)
        // findNavController().navigate(...)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
