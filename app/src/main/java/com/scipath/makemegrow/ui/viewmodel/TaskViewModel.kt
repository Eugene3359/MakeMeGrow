package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.scipath.makemegrow.data.common.CategoryIds.ALL
import com.scipath.makemegrow.data.common.CategoryIds.DEFAULT
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.data.seeder.DatabaseSeeder
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val allTasks = repository.filterByCompletion(repository.allTasks, false).asLiveData()
    val overdueTasks = repository.filterByCompletion(repository.overdueTasks, false).asLiveData()
    val todayTasks = repository.filterByCompletion(repository.todayTasks, false).asLiveData()
    val tomorrowTasks = repository.filterByCompletion(repository.tomorrowTasks, false).asLiveData()
    val thisWeekTasks = repository.filterByCompletion(repository.thisWeekTasks, false).asLiveData()
    val nextWeekTasks = repository.filterByCompletion(repository.nextWeekTasks, false).asLiveData()
    val thisMonthTasks = repository.filterByCompletion(repository.thisMonthTasks, false).asLiveData()
    val nextMonthTasks = repository.filterByCompletion(repository.nextMonthTasks, false).asLiveData()
    val laterTasks = repository.filterByCompletion(repository.laterTasks, false).asLiveData()
    val completedTasks = repository.filterByCompletion(repository.allTasks, true).asLiveData()

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
            repository.deleteTask(task)
        }
    }

    fun deleteTasks(tasks: List<Task>) {
        viewModelScope.launch {
            repository.deleteTasks(tasks)
        }
    }

    fun completeTask(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.completeTask(task, isCompleted)
        }
    }

    fun filterTasksByCategory(tasks: LiveData<List<Task>>, categoryId: Int?) : List<Task> {
        val result = when (categoryId) {
            ALL -> tasks.value
            DEFAULT -> tasks.value?.filter {
                it.categoryId == null
            }
            else -> tasks.value?.filter {
                it.categoryId == categoryId
            }
        }
        return result ?: emptyList()
    }

    fun seedDatabase() {
        viewModelScope.launch {
            repository.clear()
            DatabaseSeeder.seedTasks(repository)
        }
    }
}