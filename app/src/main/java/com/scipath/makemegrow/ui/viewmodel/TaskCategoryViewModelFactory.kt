package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scipath.makemegrow.data.repository.TaskCategoryRepository

class TaskCategoryViewModelFactory(
    private val repository: TaskCategoryRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskCategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskCategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}