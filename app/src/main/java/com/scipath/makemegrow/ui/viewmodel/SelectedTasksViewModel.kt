package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.scipath.makemegrow.data.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class SelectedTasksViewModel : ViewModel() {

    private val _selectedTasks = MutableStateFlow<List<Task>>(emptyList())
    val selectedTasks = _selectedTasks.asLiveData()

    fun select(task: Task) {
        _selectedTasks.update { it + task }
    }

    fun deselect(task: Task) {
        _selectedTasks.update { it - task }
    }

    fun toggle(task: Task) {
        if (_selectedTasks.value.contains(task)) deselect(task)
        else select(task)
    }

    fun isSelected(task: Task): Boolean {
        return task in _selectedTasks.value
    }

    fun isEmpty(): Boolean {
        return _selectedTasks.value.isEmpty()
    }

    fun clear() {
        _selectedTasks.value = emptyList()
    }
}