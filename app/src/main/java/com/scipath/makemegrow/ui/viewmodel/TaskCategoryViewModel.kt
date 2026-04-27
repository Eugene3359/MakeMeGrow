package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.scipath.makemegrow.data.model.TaskCategory
import com.scipath.makemegrow.data.repository.TaskCategoryRepository
import com.scipath.makemegrow.data.seeder.DatabaseSeeder
import kotlinx.coroutines.launch

class TaskCategoryViewModel(private val repository: TaskCategoryRepository) : ViewModel() {

    val allTaskCategories = repository.allTaskCategories.asLiveData()

    fun addCategory(category: TaskCategory) {
        viewModelScope.launch {
            repository.addCategory(category)
        }
    }

    fun updateCategory(category: TaskCategory) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: TaskCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun seedDatabase() {
        viewModelScope.launch {
            repository.clear()
            DatabaseSeeder.seedCategories(repository)
        }
    }
}