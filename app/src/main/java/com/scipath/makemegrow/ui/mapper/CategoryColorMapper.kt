package com.scipath.makemegrow.ui.mapper

import androidx.annotation.ColorRes
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.model.Category

object CategoryColorMapper {

    @ColorRes
    fun Category.Color.toResourceId(): Int {
        return when (this) {
            Category.Color.BLACK -> R.color.black
            Category.Color.RED -> R.color.red
        }
    }
}