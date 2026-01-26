package com.team.jubjub.ui.write

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentWriteLostFoundBinding

class WriteLostFoundFragment : Fragment(R.layout.fragment_write_lost_found) {

    private var _binding: FragmentWriteLostFoundBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWriteLostFoundBinding.bind(view)

        setupBack()
        setupInputFields()
        setupTopActions()
    }

    /* ------------------------
       뒤로가기
    ------------------------ */
    private fun setupBack() {
        binding.icBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    /* ------------------------
       공통 입력 다이얼로그
    ------------------------ */
    private fun showInputDialog(
        title: String,
        currentValue: String,
        onConfirm: (String) -> Unit
    ) {
        val editText = EditText(requireContext()).apply {
            setText(currentValue)
            hint = title
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("확인") { _, _ ->
                val input = editText.text.toString()
                if (input.isNotBlank()) {
                    onConfirm(input)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /* ------------------------
       추가 정보 입력 영역
    ------------------------ */
    private fun setupInputFields() {

        // 발견 장소
        binding.layoutFoundPlace.setOnClickListener {
            showInputDialog(
                title = "발견 장소",
                currentValue = binding.tvFoundPlace.text.toString()
            ) {
                binding.tvFoundPlace.text = it
            }
        }

        // 상세 장소
        binding.layoutPlaceDetail.setOnClickListener {
            showInputDialog(
                title = "상세 장소",
                currentValue = binding.tvPlaceDetail.text.toString()
            ) {
                binding.tvPlaceDetail.text = it
            }
        }

        // 발견 일시
        binding.layoutFoundDatetime.setOnClickListener {
            showInputDialog(
                title = "발견 일시",
                currentValue = binding.tvFoundDatetime.text.toString()
            ) {
                binding.tvFoundDatetime.text = it
            }
        }

        // 위탁 장소
        binding.layoutStoragePlace.setOnClickListener {
            showInputDialog(
                title = "위탁 장소",
                currentValue = binding.tvStoragePlace.text.toString()
            ) {
                binding.tvStoragePlace.text = it
            }
        }
    }

    /* ------------------------
       사진 / 장소 상단 액션
    ------------------------ */
    private fun setupTopActions() {

        // 사진 추가
        binding.layoutAddPhoto.setOnClickListener {
            Toast.makeText(requireContext(), "사진 추가 클릭", Toast.LENGTH_SHORT).show()
            // TODO: 갤러리 / 카메라 연동
        }

        // 장소 입력
        binding.layoutAddLocation.setOnClickListener {
            showInputDialog(
                title = "장소",
                currentValue = binding.tvLocation.text.toString()
            ) {
                binding.tvLocation.text = it
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
