package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import com.scipath.makemegrow.R

class TimeFormatDialog : SelectionDialog<Boolean>() {

    override val titleId: Int = R.string.time_format
    override val messageId: Int? = null
    override val options by lazy {
        listOf(
            Option(getString(R.string.twelve_hour_format), false),
            Option(getString(R.string.twenty_four_hour_format), true)
        )
    }

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "change_time_format_request"
        const val RESULT_KEY = "is_time_format_24"

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
        setResult {
            putBoolean(resultKey, selectedValue)
        }
        dismiss()
    }
}