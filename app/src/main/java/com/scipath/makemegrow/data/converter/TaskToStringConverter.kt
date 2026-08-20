package com.scipath.makemegrow.data.converter

import android.content.Context
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.model.Task

class TaskToStringConverter {
    companion object {
        fun convert(task: Task, isTimeFormat24: Boolean, context: Context): String {
            val result: StringBuilder = StringBuilder(
                context.getString(R.string.bullet_point) + task.name
            )

            if (task.deadlineDate != DateAndTimeConverter.NO_DATE) {
                val deadlineDate: String = DateAndTimeConverter.dateToString(
                    date = DateAndTimeConverter.secondsToDate(task.deadlineDate),
                    context,
                    relativeFormatting = false
                )
                result.append(" ($deadlineDate")
                if (task.deadlineTime != DateAndTimeConverter.NO_TIME) {
                    val deadlineTime: String = DateAndTimeConverter.timeToString(
                        time = DateAndTimeConverter.secondsToTime(task.deadlineTime),
                        isTimeFormat24,
                        context
                    )
                    result.append(", $deadlineTime")
                }
                result.appendLine(")")
            }

            return result.toString()
        }
    }
}