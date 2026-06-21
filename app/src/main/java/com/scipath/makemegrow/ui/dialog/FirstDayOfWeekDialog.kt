package com.scipath.makemegrow.ui.dialog

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.scipath.makemegrow.R
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale


class FirstDayOfWeekDialog : BaseDialog() {

    override val titleId: Int = R.string.first_day_of_week
    override val messageId: Int? = null
    override val inputHintId: Int? = null
    override val confirmButtonTextId: Int? = null
    override val cancelButtonTextId: Int?  = null

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    private var resultValue = DayOfWeek.MONDAY.value

    companion object {
        const val REQUEST_KEY = "change_first_day_of_week_request"
        const val RESULT_KEY = "first_day_of_week"

        private const val ARG_SELECTED_DAY = "selected_day"

        fun newInstance(selectedDay: DayOfWeek) =
            FirstDayOfWeekDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SELECTED_DAY, selectedDay.value)
                }
            }
    }

    override fun onConfirm() {
        parentFragmentManager.setFragmentResult(
            requestKey,
            Bundle().apply {
                putInt(resultKey, resultValue)
            }
        )
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val days = DayOfWeek.entries.map {
            it.getDisplayName(
                TextStyle.FULL,
                Locale.getDefault()
            )
        }
        val colorLightGray = resources.getColor(R.color.light_gray, null)
        val tintList = ColorStateList(
            arrayOf<IntArray?>(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(colorLightGray)
        )
        val textSize = resources.getDimension(R.dimen.text_medium)

        binding.radioGroup.visibility = View.VISIBLE
        days.forEach {
            val radioButton = RadioButton(context)
            radioButton.text = it
            radioButton.setTextColor(colorLightGray)
            radioButton.buttonTintList = tintList
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            binding.radioGroup.addView(radioButton)
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            val index = radioGroup.indexOfChild(radioGroup.findViewById(checkedId))
            resultValue = index + 1
        }

        val selectedDay = arguments?.getInt(ARG_SELECTED_DAY) ?: DayOfWeek.MONDAY.value
        (binding.radioGroup.getChildAt(selectedDay - 1) as RadioButton)
            .isChecked = true

        return binding.root
    }
}