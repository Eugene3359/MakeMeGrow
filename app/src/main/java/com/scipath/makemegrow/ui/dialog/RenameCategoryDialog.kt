package com.scipath.makemegrow.ui.dialog

import com.scipath.makemegrow.R

class RenameCategoryDialog : TextInputDialog() {

    override val titleId: Int = R.string.rename_category
    override val messageId: Int? = null
    override val inputHintId: Int = R.string.rename_category_hint
    override val confirmButtonTextId: Int = R.string.save
    override val cancelButtonTextId: Int? = null

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "edit_category_request"
        const val RESULT_KEY = "category_name"
    }
}