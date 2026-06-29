package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.scipath.makemegrow.R

abstract class SelectionDialog<T> : BaseDialog() {

    data class Option<T>(
        val title: String,
        val value: T
    )

    abstract val options: List<Option<T>>
    protected var defaultSelectedIndex: Int = 0
    protected var selectedOptionIndex: Int = defaultSelectedIndex
    protected val selectedValue: T
        get() = options[selectedOptionIndex].value

    companion object {
        protected const val ARG_SELECTED_OPTION = "selected_option"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding.radioGroup.visibility = View.VISIBLE
        options.forEach { option ->
            val radioButton = inflater.inflate(
                R.layout.layout_radio_button,
                binding.radioGroup,
                false
            ) as RadioButton
            radioButton.text = option.title
            binding.radioGroup.addView(radioButton)
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            selectedOptionIndex = radioGroup.indexOfChild(radioGroup.findViewById(checkedId))
        }

        selectedOptionIndex = arguments?.getInt(ARG_SELECTED_OPTION) ?: defaultSelectedIndex
        (binding.radioGroup.getChildAt(selectedOptionIndex) as RadioButton)
            .isChecked = true

        return binding.root
    }
}