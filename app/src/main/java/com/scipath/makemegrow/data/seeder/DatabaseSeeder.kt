package com.scipath.makemegrow.data.seeder

import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.repository.TaskRepository
import java.time.LocalDate

object DatabaseSeeder {

    suspend fun seedTasks(repository: TaskRepository) {
        val currentDate: LocalDate = LocalDate.now()

        repository.addTask(
            Task(0,
                "Yesterday Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.minusDays(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Today Task with Description",
                "Line 1\nLine 2\nLine 3",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Tomorrow Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusDays(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Next Week Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusDays(
                    8L - currentDate.dayOfWeek.value
                )),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Next Month Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusMonths(1).withDayOfMonth(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Next Year Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusYears(1).withMonth(1).withDayOfMonth(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )

        repository.addTask(
            Task(0,
                "Daily Repeatable Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ONCE_A_DAY,
                null)
        )

        repository.addTask(
            Task(0,
                "Mon-Fri Repeatable Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusDays(
                    if (currentDate.dayOfWeek.value < 5) 1
                    else 8L - currentDate.dayOfWeek.value
                )),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ON_WEEKDAYS,
                null)
        )

        repository.addTask(
            Task(0,
                "Sat-Sun Repeatable Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusDays(
                    if (currentDate.dayOfWeek.value == 6) 1
                    else if (currentDate.dayOfWeek.value == 7) 6
                    else 6L - currentDate.dayOfWeek.value
                )),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ON_WEEKENDS,
                null)
        )

        repository.addTask(
            Task(0,
                "Weakly Repeatable Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusDays(
                    8L - currentDate.dayOfWeek.value
                )),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ONCE_A_WEEK,
                null)
        )

        repository.addTask(
            Task(0,
                "Monthly Repeatable Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusMonths(1).withDayOfMonth(1)),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ONCE_A_MONTH,
                null)
        )

        repository.addTask(
            Task(0,
                "Yearly Repeatable Task",
                "",
                false,
                DateAndTimeConverter.dateToSeconds(
                    currentDate.plusYears(1).withMonth(1).withDayOfMonth(1)
                ),
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.ONCE_A_YEAR,
                null)
        )

        repository.addTask(
            Task(0,
                "No Deadline Task",
                "",
                false,
                DateAndTimeConverter.NO_DATE,
                DateAndTimeConverter.NO_TIME,
                Task.RepeatType.NO_REPEAT,
                null)
        )
    }

    suspend fun seedCategories(repository: CategoryRepository) {
        repository.addCategory(
            Category(0, "Work Category")
        )
    }
}