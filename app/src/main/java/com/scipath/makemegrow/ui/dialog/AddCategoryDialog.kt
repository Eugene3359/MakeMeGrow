package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import android.widget.Toast
import com.scipath.makemegrow.R

class AddCategoryDialog : BaseDialog() {

    override val titleId: Int = R.string.new_category
    override val messageId: Int? = null
    override val inputHintId: Int = R.string.new_category_hint
    override val confirmButtonTextId: Int = R.string.add
    override val cancelButtonTextId: Int? = null

    companion object {
        const val REQUEST_KEY = "add_category_request"
        const val RESULT_KEY_NAME = "category_name"
    }

    override fun onConfirm() {
        val name = input.text.toString()

        if (name.isNotEmpty()) {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply {
                    putString(RESULT_KEY_NAME, name)
                }
            )
            dismiss()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.new_category_hint,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}