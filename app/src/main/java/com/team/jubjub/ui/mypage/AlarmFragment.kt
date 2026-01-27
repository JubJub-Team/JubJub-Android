package com.team.jubjub.ui.mypage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.team.jubjub.R
import com.team.jubjub.data.repository.AuthRepository
import com.team.jubjub.databinding.FragmentAlarmBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmFragment : Fragment(R.layout.fragment_alarm) {

    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    private lateinit var alarmAdapter: AlarmAdapter
    private val viewModel: AlarmViewModel by viewModels()

    @Inject lateinit var authRepository: AuthRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlarmBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        alarmAdapter = AlarmAdapter { alarm ->
            val userId = authRepository.getCurrentUserUid() ?: return@AlarmAdapter
            viewModel.markRead(userId, alarm)
        }

        binding.rvAlarm.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alarmAdapter
        }

        observe()

        val userId = authRepository.getCurrentUserUid()
        if (userId == null) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tvEmpty.text = "로그인이 필요합니다."
            return
        }

        viewModel.load(userId)
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is AlarmUiState.Loading -> binding.tvEmpty.visibility = View.GONE
                    is AlarmUiState.Success -> {
                        alarmAdapter.submitList(state.items)
                        binding.tvEmpty.visibility =
                            if (state.items.isEmpty()) View.VISIBLE else View.GONE
                    }
                    is AlarmUiState.Error -> {
                        alarmAdapter.submitList(emptyList())
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.tvEmpty.text = state.message
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
