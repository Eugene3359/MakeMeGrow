package com.scipath.makemegrow.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "color") var color: Color
) : Serializable {
    enum class Color(val key: String) {
        BLACK("gray"),
        /*PURPLE("purple"),
        BLUE("blue"),
        GREEN("green"),*/
        RED("red"),
        /*ORANGE("orange"),
        YELLOW("yellow")*/
    }
}