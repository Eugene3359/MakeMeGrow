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
        DateAndTimeConverter.dateToSeconds(LocalDate.now()),
        DateAndTimeConverter.timeToSeconds(LocalTime.now()))
    val todayTasks: Flow<List<Task>> = taskDao.getBetweenDeadlines(
        DateAndTimeConverter.dateToSeconds(LocalDate.now()),
        DateAndTimeConverter.timeToSeconds(LocalTime.now().minusSeconds(1)),
        DateAndTimeConverter.dateToSeconds(LocalDate.now()),
        DateAndTimeConverter.NO_TIME)
    val tomorrowTasks: Flow<List<Task>> = taskDao.getByDeadlineDate(
        DateAndTimeConverter.dateToSeconds(LocalDate.now().plusDays(1)))
    val thisWeekTasks: Flow<List<Task>> = taskDao.getBetweenDeadlines(
        DateAndTimeConverter.dateToSeconds(LocalDate.now().plusDays(1)),
        DateAndTimeConverter.NO_TIME,
        DateAndTimeConverter.dateToSeconds(LocalDate.now().plusDays(
            (DayOfWeek.SUNDAY.value - LocalDate.now().dayOfWeek.value).toLong()
        )),
        DateAndTimeConverter.NO_TIME)
    val otherUpcomingTasks: Flow<List<Task>> = taskDao.getAfterDeadline(
        DateAndTimeConverter.dateToSeconds(LocalDate.now().plusDays(
            (DayOfWeek.SUNDAY.value - LocalDate.now().dayOfWeek.value).toLong()
        )),
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
}
