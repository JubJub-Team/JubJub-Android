package com.team.jubjub.ui.write

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.team.jubjub.R
import com.team.jubjub.databinding.FragmentWriteShareBinding

class WriteShareFragment : Fragment(R.layout.fragment_write_share) {

    private var _binding: FragmentWriteShareBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWriteShareBinding.bind(view)

        setupBack()
        setupTopActions()
        setupCategory()
        setupCondition()
        setupCount()
        setupMethod()
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
        inputType: Int,
        onConfirm: (String) -> Unit
    ) {
        val editText = EditText(requireContext()).apply {
            setText(currentValue)
            hint = title
            this.inputType = inputType
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
       공통 선택 다이얼로그
    ------------------------ */
    private fun showSelectDialog(
        title: String,
        items: Array<String>,
        onSelect: (String) -> Unit
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setItems(items) { _, which ->
                onSelect(items[which])
            }
            .show()
    }

    /* ------------------------
       사진 / 장소
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
                currentValue = binding.tvLocation.text.toString(),
                inputType = android.text.InputType.TYPE_CLASS_TEXT
            ) {
                binding.tvLocation.text = it
            }
        }
    }

    /* ------------------------
       카테고리
    ------------------------ */
    private fun setupCategory() {
        val categories = arrayOf("의류", "식품", "전자기기", "책", "기타")

        binding.layoutCategory.setOnClickListener {
            showSelectDialog("카테고리 선택", categories) {
                binding.tvCategory.text = it
            }
        }
    }

    /* ------------------------
       물품 상태
    ------------------------ */
    private fun setupCondition() {
        val conditions = arrayOf(
            "새 상품(미사용)",
            "사용감 없음",
            "사용감 적음",
            "사용감 많음"
        )

        binding.layoutCondition.setOnClickListener {
            showSelectDialog("물품 상태", conditions) {
                binding.tvCondition.text = it
            }
        }
    }

    /* ------------------------
       수량
    ------------------------ */
    private fun setupCount() {
        binding.layoutCount.setOnClickListener {
            showInputDialog(
                title = "수량",
                currentValue = binding.tvCount.text.toString(),
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
            ) {
                binding.tvCount.text = it
            }
        }
    }

    /* ------------------------
       나눔 방법
    ------------------------ */
    private fun setupMethod() {
        val methods = arrayOf("택배", "직거래")

        binding.layoutMethod.setOnClickListener {
            showSelectDialog("나눔 방법", methods) {
                binding.tvMethod.text = it
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
