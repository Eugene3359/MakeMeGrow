package com.scipath.makemegrow.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "is_completed") var isCompleted: Boolean,
    @ColumnInfo(name = "deadline_date") var deadlineDate: Long,
    @ColumnInfo(name = "deadline_time") var deadlineTime: Int,
    @ColumnInfo(name = "repeat") var repeat: RepeatType
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