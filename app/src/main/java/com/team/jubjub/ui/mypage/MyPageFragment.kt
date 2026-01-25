package com.team.jubjub.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.team.jubjub.R
import com.team.jubjub.data.model.enum.UserLevel
import com.team.jubjub.databinding.FragmentMyPageBinding
import com.team.jubjub.ui.post.SharePostDetailFragment

class MyPageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val levelInt = 2 // TODO: 서버/로컬에서 받아온 레벨 값으로 교체
        val userLevel = UserLevel.from(levelInt)

        binding.ivLevelBar.setImageResource(userLevel.levelBarRes)

        binding.btnGoDetail.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SharePostDetailFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
