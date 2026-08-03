package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.Task.RepeatType.NO_REPEAT
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_DAY
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_MONTH
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_WEEK
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_YEAR
import com.scipath.makemegrow.data.model.Task.RepeatType.ON_WEEKDAYS
import com.scipath.makemegrow.data.model.Task.RepeatType.ON_WEEKENDS
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.data.seeder.DatabaseSeeder
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    fun completeTask(task: Task, isCompleted: Boolean) {
        if (task.repeat == NO_REPEAT) {
            task.isCompleted = isCompleted
        } else {
            task.deadlineDate = getNextDeadline(task)
            task.isCompleted = false
        }

        updateTask(task)
    }

    private fun getNextDeadline(task: Task): Long {
        val deadlineDate: LocalDate = DateAndTimeConverter.secondsToDate(task.deadlineDate)
            ?: return task.deadlineDate

        val nextDate = when (task.repeat) {
            NO_REPEAT -> deadlineDate
            ONCE_A_DAY -> deadlineDate.plusDays(1)
            ON_WEEKDAYS -> deadlineDate.plusDays(
                if (deadlineDate.dayOfWeek.value < 5) 1
                else 8L - deadlineDate.dayOfWeek.value
            )
            ON_WEEKENDS -> deadlineDate.plusDays(
                if (deadlineDate.dayOfWeek.value == 6) 1
                else if (deadlineDate.dayOfWeek.value == 7) 6
                else 6L - deadlineDate.dayOfWeek.value
            )
            ONCE_A_WEEK -> deadlineDate.plusWeeks(1)
            ONCE_A_MONTH -> deadlineDate.plusMonths(1)
            ONCE_A_YEAR -> deadlineDate.plusYears(1)
        }

        return DateAndTimeConverter.dateToSeconds(nextDate)
    }

    fun filterTasksByCategory(tasks: LiveData<List<Task>>, category: Category?) : List<Task> {
        val result = tasks.value?.filter {
            it.categoryId == category?.id
        } ?: emptyList()
        return result
    }

    fun seedDatabase() {
        viewModelScope.launch {
            repository.clear()
            DatabaseSeeder.seedTasks(repository)
        }
    }
}