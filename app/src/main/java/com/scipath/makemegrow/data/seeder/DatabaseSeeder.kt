package com.scipath.makemegrow.data.seeder

import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.repository.TaskRepository
import java.time.LocalDate
import java.time.LocalTime

object DatabaseSeeder {

    suspend fun seed(repository: TaskRepository) {
        val currentDate: LocalDate = LocalDate.now()
        val currentTime: LocalTime = LocalTime.now()

        repository.addTask(
            Task(0,
                "Yesterday Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.minusDays(1)),
                DateAndTimeConverter.NO_TIME)
        )

        repository.addTask(
            Task(0,
                "Today Overdue Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.timeToSeconds(currentTime.minusSeconds(1)))
        )

        repository.addTask(
            Task(0,
                "Today Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate),
                DateAndTimeConverter.NO_TIME)
        )

        repository.addTask(
            Task(0,
                "Tomorrow Task",
                false,
                DateAndTimeConverter.dateToSeconds(currentDate.plusDays(1)),
                DateAndTimeConverter.NO_TIME)
        )

        repository.addTask(
            Task(0,
                "Task",
                false,
                DateAndTimeConverter.NO_DATE,
                DateAndTimeConverter.NO_TIME)
        )
    }
}