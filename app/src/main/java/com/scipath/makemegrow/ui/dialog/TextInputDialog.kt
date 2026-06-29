package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import com.scipath.makemegrow.R

abstract class TextInputDialog : BaseDialog() {

    @get:StringRes
    abstract val inputHintId: Int?

    override fun onConfirm() {
        val name = binding.input.text.toString().trim()

        if (name.isNotEmpty()) {
            setResult {
                putString(resultKey, name)
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

        inputHintId?.let {
            binding.input.visibility = View.VISIBLE
            binding.input.setHint(it)
        }

        return binding.root
    }
}