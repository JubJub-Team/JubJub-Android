package com.team.jubjub

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.team.jubjub.databinding.DialogChoiceBinding

class ConfirmDialogFragment : DialogFragment() {

    private var _binding: DialogChoiceBinding? = null
    private val binding get() = _binding!!

    private val requestKey: String by lazy { requireArguments().getString(ARG_REQUEST_KEY)!! }
    private val specKey: String by lazy { requireArguments().getString(ARG_SPEC_KEY)!! }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogChoiceBinding.inflate(layoutInflater)

        val spec = ConfirmDialogSpec.fromKey(specKey)

        binding.tvMessage.text = spec.message
        binding.btnLeft.text = spec.leftText
        binding.btnRight.text = spec.rightText

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
        private const val BUNDLE_CHOICE = "bundle_choice"

        fun newInstance(requestKey: String, specKey: String) =
            ConfirmDialogFragment().apply {
                arguments = bundleOf(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_SPEC_KEY to specKey
                )
            }

        fun readChoice(bundle: Bundle): DialogChoice {
            val name = bundle.getString(BUNDLE_CHOICE) ?: DialogChoice.RIGHT.name
            return DialogChoice.valueOf(name)
        }
    }
}
