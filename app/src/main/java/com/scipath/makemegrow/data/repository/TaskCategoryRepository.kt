package com.scipath.makemegrow.data.repository

import com.scipath.makemegrow.data.dao.TaskCategoryDao
import com.scipath.makemegrow.data.model.TaskCategory
import kotlinx.coroutines.flow.Flow

class TaskCategoryRepository(private val taskCategoryDao: TaskCategoryDao) {

    val allTaskCategories: Flow<List<TaskCategory>> = taskCategoryDao.getAll()

    fun getById(id: Int): TaskCategory {
        return taskCategoryDao.getById(id)
    }

    suspend fun getCount(): Int {
        return taskCategoryDao.getCount()
    }

    suspend fun addCategory(category: TaskCategory) {
        taskCategoryDao.insert(category)
    }

    suspend fun updateCategory(category: TaskCategory) {
        taskCategoryDao.updateTask(category)
    }

    suspend fun deleteCategory(category: TaskCategory) {
        taskCategoryDao.delete(category)
    }

    suspend fun clear() {
        taskCategoryDao.clear()
    }
}