package com.scipath.makemegrow.ui.dialog

import com.scipath.makemegrow.R

class DeleteTaskDialog : ConfirmationDialog() {

    override val titleId: Int = R.string.delete_task
    override val messageId: Int = R.string.delete_task_message
    override val inputHintId: Int? = null
    override val confirmButtonTextId: Int = R.string.delete
    override val cancelButtonTextId: Int? = null
    override val selectOptions: List<String>? = null

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "delete_task_request"
        const val RESULT_KEY = "is_confirmed"
    }
}