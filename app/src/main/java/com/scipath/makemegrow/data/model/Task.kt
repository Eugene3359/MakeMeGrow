package com.scipath.makemegrow.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scipath.makemegrow.data.converter.DateAndTimeConverter.Companion.NO_DATE
import com.scipath.makemegrow.data.converter.DateAndTimeConverter.Companion.NO_TIME
import java.io.Serializable

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("category_id")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") var name: String = "",
    @ColumnInfo(name = "description") var description: String = "",
    @ColumnInfo(name = "is_completed") var isCompleted: Boolean = false,
    @ColumnInfo(name = "deadline_date") var deadlineDate: Long = NO_DATE,
    @ColumnInfo(name = "deadline_time") var deadlineTime: Int = NO_TIME,
    @ColumnInfo(name = "repeat_type") var repeatType: RepeatType = RepeatType.NO_REPEAT,
    @ColumnInfo(name = "category_id") var categoryId: Int? = null
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

    fun isEmpty(): Boolean {
        return (id == 0 &&
                name.isEmpty() &&
                description.isEmpty() &&
                !isCompleted &&
                deadlineDate == NO_DATE &&
                deadlineTime == NO_TIME &&
                repeatType == RepeatType.NO_REPEAT &&
                categoryId == null)
    }

    fun isEqual(task: Task): Boolean {
        return (name == task.name &&
                description == task.description &&
                isCompleted == task.isCompleted &&
                deadlineDate == task.deadlineDate &&
                deadlineTime == task.deadlineTime &&
                repeatType == task.repeatType &&
                categoryId == task.categoryId)
    }
}