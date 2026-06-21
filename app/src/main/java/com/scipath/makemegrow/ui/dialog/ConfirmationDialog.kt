package com.scipath.makemegrow.ui.dialog

import android.os.Bundle

abstract class ConfirmationDialog : BaseDialog() {

    override fun onConfirm() {
        parentFragmentManager.setFragmentResult(
            requestKey,
            Bundle().apply {
                putBoolean(resultKey, true)
            }
        )
        dismiss()
    }

    override fun onCancel() {
        parentFragmentManager.setFragmentResult(
            requestKey,
            Bundle().apply {
                putBoolean(resultKey, false)
            }
        )
        dismiss()
    }
}