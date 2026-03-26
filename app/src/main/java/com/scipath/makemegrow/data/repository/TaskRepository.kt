package com.scipath.makemegrow.data.repository

import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.dao.TaskDao
import com.scipath.makemegrow.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAll()
    val overdueTasks: Flow<List<Task>> = taskDao.getBeforeDeadline(
        DateAndTimeConverter.dateToSeconds(LocalDate.now()),
        DateAndTimeConverter.timeToSeconds(LocalTime.now()))
    val upcomingTasks: Flow<List<Task>> = taskDao.getAfterDeadline(
        DateAndTimeConverter.dateToSeconds(LocalDate.now()),
        DateAndTimeConverter.timeToSeconds(LocalTime.now()))

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
