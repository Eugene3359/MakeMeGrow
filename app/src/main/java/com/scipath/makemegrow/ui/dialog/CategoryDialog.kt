package com.scipath.makemegrow.ui.dialog

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.HORIZONTAL
import android.widget.RadioButton
import android.widget.Toast
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.ui.mapper.CategoryColorMapper.toResourceId

abstract class CategoryDialog : TextInputDialog() {

    protected lateinit var category: Category
    protected var selectedColor = Category.Color.entries.first()

    companion object {
        const val ARG_CATEGORY = "category"
    }

    override fun onConfirm() {
        val name = binding.input.text.toString().trim()
        if (name.isNotEmpty()) {
            val result = category.copy(
                name = name,
                color = selectedColor
            )
            setResult {
                putSerializable(ARG_CATEGORY, result)
            }
            dismiss()
        } else {
            Toast.makeText(
                requireContext(),
                inputHintId ?: R.string.empty_string,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        if (arguments != null) {
            category = requireArguments().getSerializable(ARG_CATEGORY) as Category
            binding.input.setText(category.name)
            selectedColor = category.color
        } else {
            category = Category(
                name = "",
                color = Category.Color.entries.first()
            )
        }

        binding.radioGroup.orientation = HORIZONTAL
        binding.radioGroup.visibility = View.VISIBLE

        val strokeColor = requireContext().getColor(R.color.white)
        Category.Color.entries.forEach {
            val color = requireContext().getColor(it.toResourceId())

            val radioButton = inflater.inflate(
                R.layout.layout_color_radio_button,
                binding.radioGroup,
                false
            ) as RadioButton

            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 4 * resources.displayMetrics.density
                setStroke(
                    (1 * resources.displayMetrics.density).toInt(),
                    Color.TRANSPARENT
                )
                setColor(color)
            }

            radioButton.background = background

            radioButton.setOnCheckedChangeListener { _, isChecked ->
                background.setStroke(
                    (1 * resources.displayMetrics.density).toInt(),
                    if (isChecked) strokeColor else Color.TRANSPARENT
                )
            }

            binding.radioGroup.addView(radioButton)
        }

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            val index = radioGroup.indexOfChild(
                radioGroup.findViewById(checkedId)
            )
            selectedColor = Category.Color.entries[index]
        }

        (binding.radioGroup.getChildAt(selectedColor.ordinal) as RadioButton)
            .isChecked = true

        return binding.root
    }
}