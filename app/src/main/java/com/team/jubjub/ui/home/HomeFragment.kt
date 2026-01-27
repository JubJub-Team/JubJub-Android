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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.team.jubjub.MainActivity
import com.team.jubjub.R
import com.team.jubjub.data.model.Post
import com.team.jubjub.data.model.enums.PostType
import com.team.jubjub.databinding.FragmentHomeBinding
import com.team.jubjub.ui.mypage.AlarmFragment
import com.team.jubjub.ui.post.PostDetailFragment
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
                    bindSharing(state.sharingTop2) // 나눔 최신 2개 바인딩
                    bindLost(state.lostTop2)       // 분실 최신 2개 바인딩
                }
            }
        }
    }

    // -------------------------
    // 버튼 / 검색 기능 설정
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
        // TODO: 검색 결과 화면으로 이동 로직 구현 필요
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

    // -------------------------
    // 홈 카드 데이터 바인딩 (이미지 로딩 포함)
    // -------------------------

    private fun bindSharing(list: List<Post>) {
        val first = list.getOrNull(0)
        val second = list.getOrNull(1)

        // 데이터가 없으면 카드 숨김 처리
        binding.cvFirstBoard.visibility = if (first == null) View.GONE else View.VISIBLE
        binding.cvSecondBoard.visibility = if (second == null) View.GONE else View.VISIBLE

        // 1번 카드 데이터 바인딩
        if (first != null) {
            binding.tvFirstBoardUserName.text = first.writerNickname
            binding.tvFirstBoardTitle.text = first.title
            binding.tvFirstBoardBodyText.text = first.content
            binding.tvFirstBoardTime.text = first.createdAt?.toDate()?.let { formatDate(it) } ?: ""

            // 이미지 로딩 (썸네일)
            val imageUrl = first.images.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                binding.ivFirstBoardImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(imageUrl)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(binding.ivFirstBoardImage)
            } else {
                binding.ivFirstBoardImage.visibility = View.GONE
            }

            binding.cvFirstBoard.setOnClickListener { openDetail(first.postType, first.postId) }
        }

        // 2번 카드 데이터 바인딩
        if (second != null) {
            binding.tvSecondBoardUserName.text = second.writerNickname
            binding.tvSecondBoardTitle.text = second.title
            binding.tvSecondBoardBodyText.text = second.content
            binding.tvSecondBoardTime.text = second.createdAt?.toDate()?.let { formatDate(it) } ?: ""

            // 이미지 로딩 (썸네일)
            val imageUrl = second.images.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                binding.ivSecondBoardImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(imageUrl)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(binding.ivSecondBoardImage)
            } else {
                binding.ivSecondBoardImage.visibility = View.GONE
            }

            binding.cvSecondBoard.setOnClickListener { openDetail(second.postType, second.postId) }
        }
    }

    private fun bindLost(list: List<Post>) {
        val third = list.getOrNull(0)
        val fourth = list.getOrNull(1)

        binding.cvThirdBoard.visibility = if (third == null) View.GONE else View.VISIBLE
        binding.cvFourthBoard.visibility = if (fourth == null) View.GONE else View.VISIBLE

        // 3번 카드 데이터 바인딩
        if (third != null) {
            binding.tvThirdBoardUserName.text = third.writerNickname
            binding.tvThirdBoardTitle.text = third.title
            binding.tvThirdBoardBodyText.text = third.content
            binding.tvThirdBoardTime.text = third.createdAt?.toDate()?.let { formatDate(it) } ?: ""

            // 이미지 로딩 (썸네일)
            val imageUrl = third.images.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                binding.ivThirdBoardImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(imageUrl)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(binding.ivThirdBoardImage)
            } else {
                binding.ivThirdBoardImage.visibility = View.GONE
            }

            binding.cvThirdBoard.setOnClickListener { openDetail(third.postType, third.postId) }
        }

        // 4번 카드 데이터 바인딩
        if (fourth != null) {
            binding.tvFourthBoardUserName.text = fourth.writerNickname
            binding.tvFourthBoardTitle.text = fourth.title
            binding.tvFourthBoardBodyText.text = fourth.content
            binding.tvFourthBoardTime.text = fourth.createdAt?.toDate()?.let { formatDate(it) } ?: ""

            // 이미지 로딩 (썸네일)
            val imageUrl = fourth.images.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                binding.ivFourthBoardImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(imageUrl)
                    .transform(CenterCrop(), RoundedCorners(16))
                    .into(binding.ivFourthBoardImage)
            } else {
                binding.ivFourthBoardImage.visibility = View.GONE
            }

            binding.cvFourthBoard.setOnClickListener { openDetail(fourth.postType, fourth.postId) }
        }
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("MM/dd  HH:mm", Locale.KOREA).format(date)
    }

    private fun openDetail(postType: PostType, postId: String) {
        // 상세 화면 Fragment로 교체
        val fragment = PostDetailFragment.newInstance(postType, postId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}