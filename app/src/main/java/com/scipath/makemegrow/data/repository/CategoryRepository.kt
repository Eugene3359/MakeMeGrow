package com.scipath.makemegrow.data.repository

import com.scipath.makemegrow.data.dao.CategoryDao
import com.scipath.makemegrow.data.model.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: Flow<List<Category>> = categoryDao.getAll()

    fun getById(id: Int): Category {
        return categoryDao.getById(id)
    }

    suspend fun getCount(): Int {
        return categoryDao.getCount()
    }

    suspend fun addCategory(category: Category): Boolean {
        return categoryDao.insert(category) != -1L
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateTask(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }

    suspend fun clear() {
        categoryDao.clear()
    }
}