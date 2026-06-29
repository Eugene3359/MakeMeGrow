package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import com.scipath.makemegrow.R
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

class FirstDayOfWeekDialog : SelectionDialog<Int>() {

    override val titleId: Int = R.string.first_day_of_week
    override val messageId: Int? = null
    override val options by lazy {
        DayOfWeek.entries.map {
            Option(
                it.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                it.value
            )
        }
    }

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "change_first_day_of_week_request"
        const val RESULT_KEY = "first_day_of_week"

        fun newInstance(firstDayOfWeek: DayOfWeek) =
            FirstDayOfWeekDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SELECTED_OPTION, firstDayOfWeek.value - 1)
                }
            }
    }

    override fun onConfirm() {
        setResult {
            putInt(resultKey, selectedValue)
        }
        dismiss()
    }
}