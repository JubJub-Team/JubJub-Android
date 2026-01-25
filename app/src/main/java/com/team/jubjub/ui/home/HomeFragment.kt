package com.team.jubjub.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.team.jubjub.MainActivity
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentHomeBinding
import com.team.jubjub.ui.mypage.AlarmFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
