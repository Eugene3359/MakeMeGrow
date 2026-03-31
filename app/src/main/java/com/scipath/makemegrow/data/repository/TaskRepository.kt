package com.scipath.makemegrow.data.repository

import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.dao.TaskDao
import com.scipath.makemegrow.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAll()
    val overdueTasks: Flow<List<Task>> = taskDao.getBeforeDeadline(
        DateAndTimeConverter.dateToSeconds(currentDate()),
        DateAndTimeConverter.timeToSeconds(currentTime()))
    val todayTasks: Flow<List<Task>> = taskDao.getBetweenDeadlines(
        DateAndTimeConverter.dateToSeconds(currentDate()),
        DateAndTimeConverter.timeToSeconds(currentTime().minusSeconds(1)),
        DateAndTimeConverter.dateToSeconds(currentDate()),
        DateAndTimeConverter.NO_TIME)
    val tomorrowTasks: Flow<List<Task>> = taskDao.getByDeadlineDate(
        DateAndTimeConverter.dateToSeconds(tomorrowDate()))
    val thisWeekTasks: Flow<List<Task>> = taskDao.getBetweenDeadlines(
        DateAndTimeConverter.dateToSeconds(tomorrowDate()),
        DateAndTimeConverter.NO_TIME,
        DateAndTimeConverter.dateToSeconds(endOfThisWeek()),
        DateAndTimeConverter.NO_TIME)
    val nextWeekTasks: Flow<List<Task>> = taskDao.getBetweenDeadlines(
        DateAndTimeConverter.dateToSeconds(endOfThisWeek()),
        DateAndTimeConverter.NO_TIME,
        DateAndTimeConverter.dateToSeconds(endOfNextWeek()),
        DateAndTimeConverter.NO_TIME)
    val thisMonthTasks: Flow<List<Task>> = taskDao.getBetweenDeadlines(
        DateAndTimeConverter.dateToSeconds(endOfNextWeek()),
        DateAndTimeConverter.NO_TIME,
        DateAndTimeConverter.dateToSeconds(endOfThisMonth()),
        DateAndTimeConverter.NO_TIME)
    val laterTasks: Flow<List<Task>> = taskDao.getAfterDeadline(
        DateAndTimeConverter.dateToSeconds(
            if (endOfThisMonth().isAfter(endOfNextWeek()))
                endOfThisMonth()
            else
                endOfNextWeek()),
        DateAndTimeConverter.NO_TIME)

    fun getById(id: Int): Task {
        return taskDao.getById(id);
    }

    suspend fun addTask(task: Task) {
        taskDao.insert(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
    }

    suspend fun clear() {
        taskDao.clear()
    }

    private fun currentDate(): LocalDate {
        return LocalDate.now()
    }

    private fun currentTime(): LocalTime {
        return LocalTime.now()
    }

    private fun tomorrowDate(): LocalDate {
        return currentDate().plusDays(1)
    }

    private fun endOfThisWeek(): LocalDate {
        return currentDate().plusDays(
            (DayOfWeek.SUNDAY.value - currentDate().dayOfWeek.value).toLong()
        )
    }

    private fun endOfNextWeek(): LocalDate {
        return endOfThisWeek().plusDays(7)
    }

    private fun endOfThisMonth(): LocalDate {
        return currentDate().plusDays(
            (currentDate().month.length(currentDate().isLeapYear) - currentDate().dayOfMonth).toLong())
    }
}
