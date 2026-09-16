package com.scipath.makemegrow.data.converter

import androidx.room.TypeConverter
import com.scipath.makemegrow.data.model.Category

class CategoryColorConverter {

    @TypeConverter
    fun fromColor(color: Category.Color): String {
        return color.key
    }

    @TypeConverter
    fun toColor(key: String): Category.Color {
        return Category.Color.entries.firstOrNull { it.key == key }
            ?: Category.Color.BLACK
    }
}