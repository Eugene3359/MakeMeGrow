package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.seeder.DatabaseSeeder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _selectedCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    val allCategories = repository.allCategories.asLiveData()
    val selectedCategoryId = repository.selectedCategoryId.asLiveData()
    val selectedCategoryIds = _selectedCategoryIds.asLiveData()

    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.addCategory(category)
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

    fun selectCategory(categoryId: Int) {
        repository.setSelectedCategory(categoryId)
    }

    fun addSelectedCategory(id: Int) {
        _selectedCategoryIds.value += id
    }

    fun removeSelectedCategory(id: Int) {
        _selectedCategoryIds.value -= id
    }

    fun deleteSelectedCategories() {
        val selectedIds = _selectedCategoryIds.value
        allCategories.value
            ?.filter { it.id in selectedIds }
            ?.forEach(::deleteCategory)
        clearSelectedCategories()
    }

    fun clearSelectedCategories() {
        _selectedCategoryIds.value = emptySet()
    }

    fun seedDatabase() {
        viewModelScope.launch {
            repository.clear()
            DatabaseSeeder.seedCategories(repository)
        }
    }
}