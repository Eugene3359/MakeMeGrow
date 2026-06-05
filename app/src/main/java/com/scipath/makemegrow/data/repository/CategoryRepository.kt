package com.scipath.makemegrow.data.repository

import com.scipath.makemegrow.data.common.CategoryIds.ALL
import com.scipath.makemegrow.data.dao.CategoryDao
import com.scipath.makemegrow.data.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoryRepository(private val categoryDao: CategoryDao) {

    private val _selectedCategoryId = MutableStateFlow(ALL)
    val allCategories: Flow<List<Category>> = categoryDao.getAll()
    val selectedCategoryId: StateFlow<Int> = _selectedCategoryId.asStateFlow()

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

    fun setSelectedCategory(id: Int) {
        if (_selectedCategoryId.value != id)
            _selectedCategoryId.value = id
    }

    suspend fun clear() {
        categoryDao.clear()
    }
}