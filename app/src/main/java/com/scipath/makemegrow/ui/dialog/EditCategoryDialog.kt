package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.model.Category

class EditCategoryDialog : CategoryDialog() {

    override val titleId: Int = R.string.rename_category
    override val messageId: Int? = null
    override val inputHintId: Int = R.string.rename_category_hint
    override val confirmButtonTextId: Int = R.string.save

    override val requestKey: String = REQUEST_KEY
    override val resultKey: String = RESULT_KEY

    companion object {
        const val REQUEST_KEY = "edit_category_request"
        const val RESULT_KEY = ARG_CATEGORY

        fun newInstance(category: Category) =
            EditCategoryDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORY, category)
                }
            }
    }
}