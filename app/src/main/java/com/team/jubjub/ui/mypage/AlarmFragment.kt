package com.team.jubjub.ui.mypage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment(R.layout.fragment_alarm) {

    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    private lateinit var alarmAdapter: AlarmAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlarmBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        alarmAdapter = AlarmAdapter { alarm ->
            // TODO: 클릭 시 읽음 처리
            // 예: viewModel.markRead(alarm.id)
        }

        binding.rvAlarm.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = alarmAdapter
        }

        // 더미 데이터 (최신이 위로 보이게 createdAtMillis 조절)
        val alarms = listOf(
            Alarm(
                id = 0,
                title = "오늘 들어온 알림 테스트",
                createdAtMillis = System.currentTimeMillis() - 10 * 60 * 1000, // 10분 전
                isRead = false
            ),
            Alarm(
                id = 1,
                title = "분실물 이름... 이 찾기 완료되었습니다",
                createdAtMillis = System.currentTimeMillis() - 24L * 60 * 60 * 1000, // 1일 전
                isRead = false
            ),
            Alarm(
                id = 2,
                title = "분실물 이름... 에 댓글이 달렸습니다.",
                createdAtMillis = System.currentTimeMillis() - 6L * 24 * 60 * 60 * 1000, // 6일 전
                isRead = true
            ),
            Alarm(
                id = 3,
                title = "나눔 물품 이동... 줌줌 메시지가 도착했습니다",
                createdAtMillis = System.currentTimeMillis() - 20L * 24 * 60 * 60 * 1000, // 20일 전
                isRead = true
            )
        )

        val uiItems = buildAlarmUiItems(alarms)
        alarmAdapter.submitList(uiItems)

        binding.tvEmpty.visibility = if (uiItems.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
