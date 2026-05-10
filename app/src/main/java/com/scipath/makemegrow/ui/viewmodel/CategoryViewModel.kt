package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.seeder.DatabaseSeeder
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    val allCategories = repository.allCategories.asLiveData()

    fun addCategory(
        category: Category,
        onResult: ((Boolean) -> Unit)?
    ) {
        viewModelScope.launch {
            val isSuccessful = repository.addCategory(category)
            onResult?.invoke(isSuccessful)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
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