package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import android.widget.Toast
import com.scipath.makemegrow.R

abstract class TextInputDialog : BaseDialog() {

    override fun onConfirm() {
        val name = binding.input.text.toString()

        if (name.isNotEmpty()) {
            parentFragmentManager.setFragmentResult(
                requestKey,
                Bundle().apply {
                    putString(resultKey, name)
                }
            )
            dismiss()
        } else {
            Toast.makeText(
                requireContext(),
                inputHintId ?: R.string.empty_string,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}