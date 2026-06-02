package com.scipath.makemegrow.ui.dialog

import com.scipath.makemegrow.R

class TaskCompletionDialog : ConfirmationDialog() {
    override val titleId: Int = R.string.complete_task
    override val messageId: Int? = null
    override val inputHintId: Int? = null
    override val confirmButtonTextId: Int? = null
    override val cancelButtonTextId: Int? = null

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "complete_task_request"
        const val RESULT_KEY = "is_confirmed"
    }
}