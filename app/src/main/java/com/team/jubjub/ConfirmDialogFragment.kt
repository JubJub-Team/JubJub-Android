package com.team.jubjub

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.team.jubjub.databinding.DialogChoiceBinding

class ConfirmDialogFragment : DialogFragment() {
    private var _binding: DialogChoiceBinding? = null
    private val binding get() = _binding!!

    private val requestKey: String by lazy { requireArguments().getString(ARG_REQUEST_KEY)!! }
    private val specKey: String by lazy { requireArguments().getString(ARG_SPEC_KEY)!! }

    private val themeColor: Int by lazy {
        val color = requireArguments().getInt(ARG_THEME_COLOR, -1)
        if (color != -1) color else ContextCompat.getColor(requireContext(), R.color.main)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogChoiceBinding.inflate(layoutInflater)
        val spec = ConfirmDialogSpec.fromKey(specKey)

        binding.tvMessage.text = spec.message
        binding.btnLeft.text = spec.leftText
        binding.btnRight.text = spec.rightText

        // 색상 변수 준비
        val mainColor = ContextCompat.getColor(requireContext(), R.color.main)
        val blueColor = Color.parseColor("#1E88E5")
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)

        // 게시글 선택 다이얼로그(PostType)일 때만 특별 색상 적용
        if (specKey == "postType") {
            // 1. 왼쪽 버튼: 파란색 배경 + 흰색 글씨
            binding.btnLeft.backgroundTintList = ColorStateList.valueOf(blueColor)
            binding.btnLeft.setTextColor(whiteColor)

            // 2. 오른쪽 버튼: 메인 색상 배경 + 흰색 글씨
            binding.btnRight.backgroundTintList = ColorStateList.valueOf(mainColor)
            binding.btnRight.setTextColor(whiteColor)
            binding.btnRight.strokeWidth = 0 // 테두리 제거 (글자 가독성 위해)
        } else {
            // 일반 다이얼로그 로직 (기본 themeColor 적용)
            val colorStateList = ColorStateList.valueOf(themeColor)
            binding.btnLeft.backgroundTintList = colorStateList
            binding.btnRight.backgroundTintList = ColorStateList.valueOf(whiteColor)
            binding.btnRight.setTextColor(themeColor)
            binding.btnRight.strokeColor = colorStateList
        }

        binding.btnLeft.setOnClickListener { sendResult(DialogChoice.LEFT) }
        binding.btnRight.setOnClickListener { sendResult(DialogChoice.RIGHT) }

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(true)
        }
    }

    private fun sendResult(choice: DialogChoice) {
        parentFragmentManager.setFragmentResult(
            requestKey,
            bundleOf(BUNDLE_CHOICE to choice.name)
        )
        dismiss()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val ARG_SPEC_KEY = "arg_spec_key"
        private const val ARG_THEME_COLOR = "arg_theme_color"
        private const val BUNDLE_CHOICE = "bundle_choice"

        fun newInstance(requestKey: String, specKey: String, themeColor: Int? = null) =
            ConfirmDialogFragment().apply {
                arguments = bundleOf(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_SPEC_KEY to specKey,
                    ARG_THEME_COLOR to (themeColor ?: -1)
                )
            }

        fun readChoice(bundle: Bundle): DialogChoice {
            val name = bundle.getString(BUNDLE_CHOICE) ?: DialogChoice.RIGHT.name
            return DialogChoice.valueOf(name)
        }
    }
}