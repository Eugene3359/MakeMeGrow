package com.scipath.makemegrow.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.databinding.ActivityMainBinding
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.dialog.DeleteTasksDialog
import com.scipath.makemegrow.ui.dialog.TaskCompletionDialog
import com.scipath.makemegrow.ui.manager.MainTaskBarManager
import com.scipath.makemegrow.ui.manager.TaskSectionManager
import com.scipath.makemegrow.ui.viewmodel.SelectedTasksViewModel
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEV_MODE = true
    }

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var selectedTasksViewModel: SelectedTasksViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var taskBarManager: MainTaskBarManager
    private lateinit var taskSectionManager: TaskSectionManager
    private var pendingTask: Task? = null
    private var onTaskCompletionCancel: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as MakeMeGrowApp
        taskViewModel = ViewModelProvider(this, app.taskFactory)[TaskViewModel::class.java]
        selectedTasksViewModel = ViewModelProvider(this)[SelectedTasksViewModel::class.java].apply {
            selectedTasks.observe(this@MainActivity) {
                taskBarManager.updateVisibilities(!selectedTasksViewModel.isEmpty())
            }
        }
        categoryViewModel = ViewModelProvider(this, app.categoryFactory)[CategoryViewModel::class.java].apply {
            selectedCategoryId.observe(this@MainActivity) {
                taskBarManager.updateCategorySpinner()
                taskSectionManager.updateSections()
            }
        }
        settingsViewModel = ViewModelProvider(this, app.settingsFactory)[SettingsViewModel::class.java].apply {
            confirmationOfCompletion.observe(this@MainActivity) {}
            timeFormat24.observe(this@MainActivity) {}
        }

        if (DEV_MODE && savedInstanceState == null) {
            categoryViewModel.seedDatabase()
            taskViewModel.seedDatabase()
        }

        taskBarManager = MainTaskBarManager(
            activity = this,
            binding = binding,
            selectedTasksViewModel = selectedTasksViewModel,
            categoryViewModel = categoryViewModel,
            settingsViewModel = settingsViewModel,
            onCompletionFilterChange = ::onCompletionFilterChange,
            deselectTasks = ::deselectTasks
        )

        taskSectionManager = TaskSectionManager(
            activity = this,
            binding = binding,
            taskViewModel = taskViewModel,
            selectedTasksViewModel = selectedTasksViewModel,
            categoryViewModel = categoryViewModel,
            settingsViewModel = settingsViewModel,
            onTaskClick = ::onTaskClick,
            onTaskCheck = ::onTaskCheck
        )

        taskBarManager.setupTaskbar()
        taskSectionManager.setupSections()
        setupDialogListeners()

        binding.buttonNewTask.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }
    }

    private fun onTaskClick(task: Task) {
        val intent = Intent(this, TaskActivity::class.java).apply {
            putExtra("task", task)
        }
        startActivity(intent)
    }

    private fun onTaskCheck(task: Task, isChecked: Boolean, onCancel: () -> Unit) {
        if (settingsViewModel.isConfirmationOfCompletionEnabled() && isChecked) {
            pendingTask = task
            onTaskCompletionCancel = onCancel
            TaskCompletionDialog().show(supportFragmentManager, "TaskCompletionDialog")
        } else {
            taskViewModel.completeTask(task, isChecked)
        }
    }

    private fun onCompletionFilterChange(isChecked: Boolean) {
        taskSectionManager.displayCompletedTasks = isChecked
        taskSectionManager.updateSections()
    }

    private fun deselectTasks() {
        taskSectionManager.deselectTasks()
    }

    private fun setupDialogListeners() {
        // Add Category
        supportFragmentManager.setFragmentResultListener(
            AddCategoryDialog.REQUEST_KEY,
            this,
            { _, bundle ->
                val category = bundle.getSerializable(
                    AddCategoryDialog.RESULT_KEY
                ) as Category
                categoryViewModel.addCategory(category)
            }
        )

        // Complete Task
        supportFragmentManager.setFragmentResultListener(
            TaskCompletionDialog.REQUEST_KEY,
            this,
            { _, bundle ->
                val isConfirmed = bundle.getBoolean(TaskCompletionDialog.RESULT_KEY)
                if (isConfirmed) {
                    pendingTask?.let { task ->
                        taskViewModel.completeTask(task, true)
                    }
                } else {
                    onTaskCompletionCancel?.invoke()
                }
                pendingTask = null
                onTaskCompletionCancel = null
            }
        )

        // Delete Tasks
        supportFragmentManager.setFragmentResultListener(
            DeleteTasksDialog.REQUEST_KEY,
            this,
            { _, bundle ->
                val isConfirmed = bundle.getBoolean(DeleteTasksDialog.RESULT_KEY)
                if (isConfirmed) {
                    selectedTasksViewModel.selectedTasks.value?.let {
                        taskViewModel.deleteTasks(it)
                        deselectTasks()
                    }
                }
            }
        )
    }
}