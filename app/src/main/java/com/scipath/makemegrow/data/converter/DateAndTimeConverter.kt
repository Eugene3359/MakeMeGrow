package com.scipath.makemegrow.data.converter

import android.content.Context
import com.scipath.makemegrow.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateAndTimeConverter {
    companion object {
        const val SECONDS_IN_DAY = 86400
        const val NO_DATE = Long.MAX_VALUE
        const val NO_TIME = Int.MAX_VALUE

        fun dateToSeconds(date: LocalDate?): Long {
            if (date == null) return NO_DATE
            return date.toEpochDay() * SECONDS_IN_DAY // Convert days to seconds
        }

        fun timeToSeconds(time: LocalTime?): Int {
            return time?.toSecondOfDay() ?: NO_TIME
        }

        fun secondsToDate(seconds: Long): LocalDate? {
            if (seconds == NO_DATE) return null
            return LocalDate.ofEpochDay(seconds / SECONDS_IN_DAY) // Convert seconds to days
        }

        fun secondsToTime(secondOfDay: Int): LocalTime? {
            if (secondOfDay == NO_TIME) return null
            return LocalTime.ofSecondOfDay(secondOfDay.toLong())
        }

        fun dateToString(date: LocalDate?, context: Context): String {
            val currentDate: LocalDate = LocalDate.now()
            if (date == currentDate.minusDays(1))
                return context.getString(R.string.yesterday)
            if (date == currentDate)
                return context.getString(R.string.today)
            if (date == currentDate.plusDays(1))
                return context.getString(R.string.tomorrow)
            val formatter = DateTimeFormatter.ofPattern(
                context.getString(R.string.date_format),
                Locale.getDefault())
            return date?.format(formatter) ?: ""
        }

        fun timeToString(time: LocalTime?, context: Context): String {
            val formatter = DateTimeFormatter.ofPattern(
                context.getString(R.string.time_format),
                Locale.getDefault())
            return time?.format(formatter) ?: ""
        }

        fun dateAndTimeToString(date: LocalDate?, time: LocalTime?, context: Context): String {
            if (date == null) return ""
            if (time == null) return dateToString(date, context)
            return context.getString(R.string.date_time_formatting).format(
                dateToString(date, context),
                timeToString(time, context))
        }
    }
}