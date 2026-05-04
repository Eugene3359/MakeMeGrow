package com.scipath.makemegrow.data.seeder

import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.repository.TaskRepository
import java.time.LocalDate
import java.time.LocalTime

object DatabaseSeeder {

    suspend fun seedTasks(repository: TaskRepository) {
        val currentDate: LocalDate = LocalDate.now()
        val currentTime: LocalTime = LocalTime.now()

        repository.addTask(
            Task(0,
                "Yesterday Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.minusDays(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Today Overdue Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.timeToSeconds(currentTime.minusSeconds(1)),
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Today Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Tomorrow Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusDays(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Next Week Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusWeeks(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Next Month Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusMonths(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Next Year Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusYears(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Once a Day Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ONCE_A_DAY,
                null)
        )

        repository.addTask(
            Task(0,
                "Mon-Fri Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ON_WEEKDAYS,
                null)
        )

        repository.addTask(
            Task(0,
                "Sat-Sun Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ON_WEEKENDS,
                null)
        )

        repository.addTask(
            Task(0,
                "Once a Week Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ONCE_A_WEEK,
                null)
        )

        repository.addTask(
            Task(0,
                "Once a Month Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ONCE_A_MONTH,
                null)
        )

        repository.addTask(
            Task(0,
                "Once a Year Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ONCE_A_YEAR,
                null)
        )

        repository.addTask(
            Task(0,
                "Task",
                false,
                DateAndTimeConverter.NO_DATE,
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )
    }

    suspend fun seedCategories(repository: CategoryRepository) {
        repository.addCategory(
            Category(0,
                "Work Category")
        )
    }
}