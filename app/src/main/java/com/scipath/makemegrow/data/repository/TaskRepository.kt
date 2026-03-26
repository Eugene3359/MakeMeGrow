package com.scipath.makemegrow.data.repository

import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.dao.TaskDao
import com.scipath.makemegrow.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAll()
    val overdueTasks: Flow<List<Task>> = taskDao.getBeforeDeadlineDate(
        DateAndTimeConverter.dateToSeconds(LocalDate.now()))
    val upcomingTasks: Flow<List<Task>> = taskDao.getAfterDeadlineDate(
        DateAndTimeConverter.dateToSeconds(LocalDate.now().minusDays(1)))

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
}
