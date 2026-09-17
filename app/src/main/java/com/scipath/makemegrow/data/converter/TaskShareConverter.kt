package com.scipath.makemegrow.data.converter

import android.content.Context
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.model.Task

object TaskShareConverter {

    fun Task.toShareString(isTimeFormat24: Boolean, context: Context) : String {
        val result: StringBuilder = StringBuilder(
            context.getString(R.string.bullet_point) + name
        )

        if (deadlineDate != DateAndTimeConverter.NO_DATE) {
            val deadlineDate: String = DateAndTimeConverter.dateToString(
                date = DateAndTimeConverter.secondsToDate(deadlineDate),
                context,
                relativeFormatting = false
            )
            result.append(" ($deadlineDate")
            if (deadlineTime != DateAndTimeConverter.NO_TIME) {
                val deadlineTime: String = DateAndTimeConverter.timeToString(
                    time = DateAndTimeConverter.secondsToTime(deadlineTime),
                    isTimeFormat24,
                    context
                )
                result.append(", $deadlineTime")
            }
            result.appendLine(")")
        }

        return result.toString()
    }

    fun List<Task>.toShareString(isTimeFormat24: Boolean, context: Context) : String {
        return sortedWith(
            compareBy<Task> { it.deadlineDate }
                .thenBy { it.deadlineTime }
                .thenBy { it.name }
        ).joinToString(separator = "") { task ->
            task.toShareString(isTimeFormat24, context)
        }
    }
}