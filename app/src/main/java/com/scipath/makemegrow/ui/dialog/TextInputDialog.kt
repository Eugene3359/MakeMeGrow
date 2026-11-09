package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.scipath.makemegrow.R
import com.scipath.makemegrow.ui.toast.AppToast

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
            AppToast.makeText(
                requireActivity(),
                getString(inputHintId ?: R.string.empty_string),
                requireDialog().window!!.decorView
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