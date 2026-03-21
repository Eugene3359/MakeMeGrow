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
    @ColumnInfo(name = "deadline_time") var deadlineTime: Int
) : Serializable