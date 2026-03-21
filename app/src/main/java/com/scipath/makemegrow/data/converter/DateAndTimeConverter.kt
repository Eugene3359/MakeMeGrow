package com.scipath.makemegrow.data.converter

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
            if (secondOfDay < 0) return null
            return LocalTime.ofSecondOfDay(secondOfDay.toLong())
        }

        fun secondsToDateTime(seconds: Long): LocalDateTime {
            return Instant
                .ofEpochSecond(seconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }

        fun dateToString(date: LocalDate?): String {
            val formatter = DateTimeFormatter
                .ofPattern("dd MMM yyyy", Locale.getDefault())
            return date?.format(formatter) ?: ""
        }

        fun timeToString(time: LocalTime?): String {
            val formatter = DateTimeFormatter
                .ofPattern("HH:mm", Locale.getDefault())
            return time?.format(formatter) ?: ""
        }

        fun dateTimeToString(dateTime: LocalDateTime?): String {
            val formatter = DateTimeFormatter
                .ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
            return dateTime?.format(formatter) ?: ""
        }
    }
}