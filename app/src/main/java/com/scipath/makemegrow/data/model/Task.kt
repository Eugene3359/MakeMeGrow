package com.scipath.makemegrow.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")])
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "is_completed") var isCompleted: Boolean,
    @ColumnInfo(name = "deadline_date") var deadlineDate: Long,
    @ColumnInfo(name = "deadline_time") var deadlineTime: Int,
    @ColumnInfo(name = "repeat") var repeat: RepeatType,
    @ColumnInfo(name = "categoryId") var categoryId: Int? = null
) : Serializable {
    enum class RepeatType {
        NO_REPEAT,
        ONCE_A_DAY,
        ON_WEEKDAYS,
        ON_WEEKENDS,
        ONCE_A_WEEK,
        ONCE_A_MONTH,
        ONCE_A_YEAR
    }
}