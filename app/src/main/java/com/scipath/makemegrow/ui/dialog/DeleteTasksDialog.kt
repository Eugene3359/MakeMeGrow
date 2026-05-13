package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import com.scipath.makemegrow.R

class DeleteTasksDialog() : BaseDialog() {
    override val titleId: Int = R.string.delete_tasks
    override val messageId: Int = R.string.delete_tasks_message
    override val inputHintId: Int? = null
    override val confirmButtonTextId: Int = R.string.delete
    override val cancelButtonTextId: Int? = null

    companion object {
        const val REQUEST_KEY = "delete_tasks_request"
        const val RESULT_KEY_NAME = "is_confirmed"
    }

    override fun onConfirm() {
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            Bundle().apply {
                putBoolean(RESULT_KEY_NAME, true)
            }
        )
        dismiss()
    }
}