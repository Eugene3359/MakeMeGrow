package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.data.seeder.DatabaseSeeder
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val allTasks = repository.allTasks.asLiveData()
    val overdueTasks = repository.overdueTasks.asLiveData()
    val todayTasks = repository.todayTasks.asLiveData()
    val tomorrowTasks = repository.tomorrowTasks.asLiveData()
    val thisWeekTasks = repository.thisWeekTasks.asLiveData()
    val nextWeekTasks = repository.nextWeekTasks.asLiveData()
    val laterTasks = repository.laterTasks.asLiveData()

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task);
        }
    }

    fun seedDatabase() {
        viewModelScope.launch {
            repository.clear()
            DatabaseSeeder.seed(repository)
        }
    }
}