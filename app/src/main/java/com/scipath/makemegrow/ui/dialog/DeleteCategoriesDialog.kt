package com.scipath.makemegrow.ui.dialog

import com.scipath.makemegrow.R

class DeleteCategoriesDialog : ConfirmationDialog() {

    override val titleId: Int = R.string.delete_categories
    override val messageId: Int = R.string.delete_categories_message
    override val confirmButtonTextId: Int = R.string.delete

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "delete_categories_request"
        const val RESULT_KEY = "is_confirmed"
    }
}