package com.scipath.makemegrow.ui.dialog

abstract class ConfirmationDialog : BaseDialog() {

    override fun onConfirm() {
        setResult {
            putBoolean(resultKey, true)
        }
        dismiss()
    }

    override fun onCancel() {
        setResult {
            putBoolean(resultKey, false)
        }
        dismiss()
    }
}