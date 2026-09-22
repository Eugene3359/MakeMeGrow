package com.scipath.makemegrow.ui.mapper

import androidx.annotation.ColorRes
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.model.Category

object CategoryColorMapper {

    @ColorRes
    fun Category.Color.toResourceId(): Int {
        return when (this) {
            Category.Color.BLACK -> R.color.black
            Category.Color.PURPLE -> R.color.purple
            Category.Color.BLUE -> R.color.blue
            Category.Color.GREEN -> R.color.green
            Category.Color.RED -> R.color.red
            Category.Color.ORANGE -> R.color.orange
            Category.Color.YELLOW -> R.color.yellow
        }
    }
}