package com.scipath.makemegrow.ui.dialog

import com.scipath.makemegrow.R

class AddCategoryDialog : TextInputDialog() {

    override val titleId: Int = R.string.new_category
    override val messageId: Int? = null
    override val inputHintId: Int = R.string.new_category_hint
    override val confirmButtonTextId: Int = R.string.add

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "add_category_request"
        const val RESULT_KEY = "category_name"
    }
}