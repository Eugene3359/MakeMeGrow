package com.scipath.makemegrow.ui.dialog

import com.scipath.makemegrow.R

class QuitDialog : ConfirmationDialog() {

    override val titleId: Int = R.string.quit
    override val messageId: Int = R.string.quit_without_saving
    override val confirmButtonTextId: Int = R.string.confirm

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "quit_without_saving"
        const val RESULT_KEY = "is_confirmed"
    }
}