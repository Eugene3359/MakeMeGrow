package com.scipath.makemegrow.data.repository

import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.dao.TaskDao
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.notification.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class TaskRepository(
    private val taskDao: TaskDao,
    private val notificationScheduler: NotificationScheduler
) {

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
    val nextMonthTasks: Flow<List<Task>> = taskDao.getBetweenDeadlines(
        DateAndTimeConverter.dateToSeconds(
            if (endOfThisMonth().isAfter(endOfNextWeek()))
                endOfThisMonth()
            else
                endOfNextWeek()),
        DateAndTimeConverter.NO_TIME,
        DateAndTimeConverter.dateToSeconds(endOfNextMonth()),
        DateAndTimeConverter.NO_TIME)
    val laterTasks: Flow<List<Task>> = taskDao.getAfterDeadline(
        DateAndTimeConverter.dateToSeconds(endOfNextMonth()),
        DateAndTimeConverter.NO_TIME)

    fun getById(id: Int): Task? {
        return taskDao.getById(id)
    }

    suspend fun addTask(task: Task) {
        val id = taskDao.insert(task)
        val savedTask = task.copy(id = id.toInt())
        notificationScheduler.schedule(savedTask)
    }

    suspend fun updateTask(task: Task) {
        notificationScheduler.cancel(task.id)
        taskDao.updateTask(task)
        notificationScheduler.schedule(task)
    }

    suspend fun upsertTask(task: Task) {
        notificationScheduler.cancel(task.id)
        val id = taskDao.upsertTask(task)
        val savedTask = task.copy(id = id.toInt())
        notificationScheduler.schedule(savedTask)
    }

    suspend fun deleteTask(task: Task) {
        notificationScheduler.cancel(task.id)
        taskDao.delete(task)
    }

    suspend fun deleteTasks(tasks: List<Task>) {
        tasks.forEach {
            notificationScheduler.cancel(it.id)
        }
        taskDao.delete(tasks)
    }

    suspend fun clear() {
        allTasks.first().forEach {
            notificationScheduler.cancel(it.id)
        }
        taskDao.clear()
    }

    fun filterByCompletion(tasks: Flow<List<Task>>, isCompleted: Boolean): Flow<List<Task>> {
        return tasks.map { list ->
            list.filter { it.isCompleted == isCompleted }
        }
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
        return endOfThisWeek().plusWeeks(1)
    }

    private fun endOfThisMonth(): LocalDate {
        return currentDate()
            .withDayOfMonth(currentDate().month.length(currentDate().isLeapYear))
    }

    private fun endOfNextMonth(): LocalDate {
        val nextMonth: LocalDate = currentDate().plusMonths(1)
        return nextMonth
            .withDayOfMonth(nextMonth.month.length(nextMonth.isLeapYear))
    }
}
