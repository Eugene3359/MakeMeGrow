package com.scipath.makemegrow.ui.manager

import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.databinding.ActivityMainBinding
import com.scipath.makemegrow.ui.adapter.TaskAdapter
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel

class TaskSectionManager(
    private val activity: AppCompatActivity,
    private val taskViewModel: TaskViewModel,
    private val categoryViewModel: CategoryViewModel,
    private val settingsViewModel: SettingsViewModel,
    private val binding: ActivityMainBinding,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskSelect: (Task, Boolean) -> Unit,
    private val onTaskCheck: (Task, Boolean, () -> Unit) -> Unit
) {

    data class TaskSection(
        val liveData: LiveData<List<Task>>,
        val parentLayout: LinearLayout,
        val recyclerView: RecyclerView,
        val adapter: TaskAdapter
    )

    private fun createTaskSection(
        liveData: LiveData<List<Task>>,
        parentLayout: LinearLayout,
        recyclerView: RecyclerView
    ): TaskSection {
        val adapter = TaskAdapter(
            tasks = emptyList(),
            onTaskClick = onTaskClick,
            onTaskSelect = onTaskSelect,
            onTaskCheck = onTaskCheck
        )

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false

        return TaskSection(
            liveData,
            parentLayout,
            recyclerView,
            adapter
        )
    }

    private val taskSections: MutableList<TaskSection> = mutableListOf()

    fun setupSections(displayCompletedTasks: Boolean) {
        taskSections.clear()
        taskSections.addAll(listOf(
            // Overdue
            createTaskSection(
                taskViewModel.overdueTasks,
                binding.layoutOverdueTasks,
                binding.viewOverdueTasks
            ),
            // Today
            createTaskSection(
                taskViewModel.todayTasks,
                binding.layoutTodayTasks,
                binding.viewTodayTasks
            ),
            // Tomorrow
            createTaskSection(
                taskViewModel.tomorrowTasks,
                binding.layoutTomorrowTasks,
                binding.viewTomorrowTasks
            ),
            // This Week
            createTaskSection(
                taskViewModel.thisWeekTasks,
                binding.layoutThisWeekTasks,
                binding.viewThisWeekTasks
            ),
            // Next Week
            createTaskSection(
                taskViewModel.nextWeekTasks,
                binding.layoutNextWeekTasks,
                binding.viewNextWeekTasks
            ),
            // This Month
            createTaskSection(
                taskViewModel.thisMonthTasks,
                binding.layoutThisMonthTasks,
                binding.viewThisMonthTasks
            ),
            // Next Month
            createTaskSection(
                taskViewModel.nextMonthTasks,
                binding.layoutNextMonthTasks,
                binding.viewNextMonthTasks
            ),
            // Later
            createTaskSection(
                taskViewModel.laterTasks,
                binding.layoutLaterTasks,
                binding.viewLaterTasks
            ),
            // Completed
            createTaskSection(
                taskViewModel.completedTasks,
                binding.layoutCompletedTasks,
                binding.viewCompletedTasks
            )
        ))

        taskSections.forEach { section ->
            section.liveData.observe(activity) {
                updateSection(section, displayCompletedTasks)
            }
        }

        settingsViewModel.timeFormat24.observe(activity) { timeFormat24 ->
            taskSections.forEach { section ->
                section.adapter.updateTimeFormat(timeFormat24)
            }
        }
    }

    fun updateSections(displayCompletedTasks: Boolean) {
        taskSections.forEach { section ->
            updateSection(section, displayCompletedTasks)
        }
    }

    private fun updateSection(taskSection: TaskSection, displayCompletedTasks: Boolean) {
        val filteredTasks = taskViewModel.filterTasksByCategory(
            taskSection.liveData,
            categoryViewModel.selectedCategoryId.value
        )

        val shouldShow = filteredTasks.isNotEmpty() &&
                filteredTasks.first().isCompleted == displayCompletedTasks

        taskSection.parentLayout.visibility =
            if (shouldShow) View.VISIBLE
            else View.GONE

        taskSection.adapter.updateTasks(filteredTasks)
    }

    fun deselectTasks() {
        taskSections.forEach { section ->
            section.adapter.deselectTasks()
        }
    }
}