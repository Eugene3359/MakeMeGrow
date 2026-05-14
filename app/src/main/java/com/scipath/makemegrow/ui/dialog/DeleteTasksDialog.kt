package com.scipath.makemegrow.ui.dialog

import com.scipath.makemegrow.R

class DeleteTasksDialog() : ConfirmationDialog() {

    override val titleId: Int = R.string.delete_tasks
    override val messageId: Int = R.string.delete_tasks_message
    override val inputHintId: Int? = null
    override val confirmButtonTextId: Int = R.string.delete
    override val cancelButtonTextId: Int? = null

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "delete_tasks_request"
        const val RESULT_KEY = "is_confirmed"
    }
}