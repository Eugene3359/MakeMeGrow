package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.scipath.makemegrow.R

class TimeFormatDialog : BaseDialog() {

    override val titleId: Int = R.string.time_format
    override val messageId: Int? = null
    override val inputHintId: Int? = null
    override val confirmButtonTextId: Int? = null
    override val cancelButtonTextId: Int? = null
    override lateinit var selectOptions: List<String>

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    private var resultValue: Boolean = true

    companion object {
        const val REQUEST_KEY = "change_time_format_request"
        const val RESULT_KEY = "is_time_format_24"

        private const val ARG_SELECTED_OPTION = "selected_option"

        fun newInstance(isTimeFormat24: Boolean) =
            TimeFormatDialog().apply {
                arguments = Bundle().apply {
                    putInt(
                        ARG_SELECTED_OPTION,
                        if (isTimeFormat24) 1
                        else 0
                    )
                }
            }
    }

    override fun onConfirm() {
        parentFragmentManager.setFragmentResult(
            requestKey,
            Bundle().apply {
                putBoolean(resultKey, resultValue)
            }
        )
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        context?.let { context ->
            selectOptions = listOf(
                context.getString(R.string.twelve_hour_format),
                context.getString(R.string.twenty_four_hour_format)
            )
        }

        super.onCreateView(inflater, container, savedInstanceState)

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            val index = radioGroup.indexOfChild(radioGroup.findViewById(checkedId))
            resultValue = index == 1
        }

        val selectedOption = arguments?.getInt(ARG_SELECTED_OPTION) ?: 1
        (binding.radioGroup.getChildAt(selectedOption) as RadioButton)
            .isChecked = true

        return binding.root
    }
}