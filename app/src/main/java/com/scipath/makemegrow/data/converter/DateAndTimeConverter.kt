package com.scipath.makemegrow.data.converter

import android.content.Context
import com.scipath.makemegrow.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateAndTimeConverter {
    companion object {
        fun dateToSeconds(date: LocalDate?): Long {
            if (date == null) return 0L
            return date.toEpochDay() * 86400 // Convert days to seconds
        }

        fun timeToSeconds(time: LocalTime?): Int {
            return time?.toSecondOfDay() ?: -1
        }

        fun secondsToDate(seconds: Long): LocalDate? {
            if (seconds == 0L) return null
            return LocalDate.ofEpochDay(seconds / 86400)
        }

        fun secondsToTime(secondOfDay: Int): LocalTime? {
            if (secondOfDay == -1) return null
            return LocalTime.ofSecondOfDay(secondOfDay.toLong())
        }

        fun secondsToDateTime(seconds: Long): LocalDateTime {
            return Instant
                .ofEpochSecond(seconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
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