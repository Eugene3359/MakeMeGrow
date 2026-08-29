package com.scipath.makemegrow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.scipath.makemegrow.R
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.common.CategoryIds.ALL
import com.scipath.makemegrow.data.common.CategoryIds.DEFAULT
import com.scipath.makemegrow.data.converter.TaskToStringConverter
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.databinding.ActivityMainBinding
import com.scipath.makemegrow.ui.adapter.CategoryArrayAdapter
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.dialog.DeleteTasksDialog
import com.scipath.makemegrow.ui.dialog.TaskCompletionDialog
import com.scipath.makemegrow.ui.manager.TaskSectionManager
import com.scipath.makemegrow.ui.viewmodel.SelectedTasksViewModel
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEV_MODE = true
        private const val SPINNER_SKIP = 2
    }

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var selectedTasksViewModel: SelectedTasksViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private var pendingTask: Task? = null
    private var onTaskCompletionCancel: (() -> Unit)? = null
    private var categoryNames: List<String> = mutableListOf()
    private lateinit var binding: ActivityMainBinding
    private lateinit var taskSectionManager: TaskSectionManager
    private lateinit var categoryAdapter: CategoryArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as MakeMeGrowApp
        taskViewModel = ViewModelProvider(this, app.taskFactory)[TaskViewModel::class.java]
        selectedTasksViewModel = ViewModelProvider(this)[SelectedTasksViewModel::class.java]
        categoryViewModel = ViewModelProvider(this, app.categoryFactory)[CategoryViewModel::class.java]
        settingsViewModel = ViewModelProvider(this, app.settingsFactory)[SettingsViewModel::class.java]
        settingsViewModel.confirmationOfCompletion.observe(this) {}
        settingsViewModel.timeFormat24.observe(this) {}

        // Seed Database
        if (DEV_MODE && savedInstanceState == null) {
            categoryViewModel.seedDatabase()
            taskViewModel.seedDatabase()
        }

        taskSectionManager = TaskSectionManager(
            activity = this,
            taskViewModel = taskViewModel,
            selectedTasksViewModel = selectedTasksViewModel.apply {
                selectedTasks.observe(this@MainActivity) {
                    if (selectedTasksViewModel.isEmpty()) {
                        updateViewsOnTasksDeselected()
                    } else {
                        updateViewsOnTasksSelected()
                    }
                }
            },
            categoryViewModel = categoryViewModel,
            settingsViewModel = settingsViewModel,
            binding = binding,
            onTaskClick = { task ->
                val intent = Intent(this, TaskActivity::class.java)
                intent.putExtra("task", task)
                startActivity(intent)
            },
            onTaskCheck = { task, isChecked, onCancel ->
                if (settingsViewModel.isConfirmationOfCompletionEnabled() && isChecked) {
                    pendingTask = task
                    onTaskCompletionCancel = onCancel
                    TaskCompletionDialog().show(supportFragmentManager, "TaskCompletionDialog")
                } else {
                    taskViewModel.completeTask(task, isChecked)
                }
            }
        )

        setupTaskbar()
        taskSectionManager.setupSections()

        binding.buttonNewTask.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }
    }

    private fun setupTaskbar() {
        binding.buttonBack.setOnClickListener {
            deselectTasks()
        }

        binding.checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
            taskSectionManager.displayCompletedTasks = isChecked
            taskSectionManager.updateSections()
        }

        setupCategorySpinner()

        binding.buttonShare.setOnClickListener {
            val text = selectedTasksViewModel.selectedTasks.value
                ?.sortedWith(
                    compareBy<Task> { it.deadlineDate }
                        .thenBy { it.deadlineTime }
                        .thenBy { it.name }
                )
                ?.joinToString(separator = "") { task ->
                    TaskToStringConverter.convert(
                        task,
                        settingsViewModel.isTimeFormat24(),
                        this
                    )
                }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
            deselectTasks()
        }

        binding.buttonDelete.setOnClickListener {
            DeleteTasksDialog().show(supportFragmentManager, "DeleteTasksDialog")
        }

        binding.buttonMenu.setOnClickListener { showMenu(it) }

        setupDialogListeners()
    }

    private fun setupCategorySpinner() {
        categoryViewModel.allCategories.observe(this) { categories ->
            categoryNames = buildList {
                add(getString(R.string.all_tasks))
                add(getString(R.string.default_category))
                addAll(categories.map { it.name })
                add(getString(R.string.add_category))
            }

            categoryAdapter.clear()
            categoryAdapter.addAll(categoryNames)
            categoryAdapter.notifyDataSetChanged()
            updateCategorySpinner()
        }

        categoryViewModel.selectedCategoryId.observe(this) {
            updateCategorySpinner()
            taskSectionManager.updateSections()
        }

        categoryAdapter = CategoryArrayAdapter(this, mutableListOf())
        binding.spinnerCategory.adapter = categoryAdapter
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == categoryNames.lastIndex) {
                    // Add Category
                    updateCategorySpinner()
                    AddCategoryDialog().show(supportFragmentManager, "AddCategoryDialog")
                } else {
                    // Change Category
                    categoryViewModel.selectCategory(
                        when (position) {
                            0 -> ALL
                            1 -> DEFAULT
                            else -> categoryViewModel.allCategories.value!![position - SPINNER_SKIP].id
                        }
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateCategorySpinner() {
        val selectedPosition = binding.spinnerCategory.selectedItemPosition
        when (categoryViewModel.selectedCategoryId.value) {
            ALL -> {
                if (selectedPosition != 0)
                    binding.spinnerCategory.setSelection(0)
            }
            DEFAULT -> {
                if (selectedPosition != 1)
                    binding.spinnerCategory.setSelection(1)
            }
            else -> {
                var categoryId: Int = ALL
                categoryViewModel.allCategories.value?.let { categories ->
                    val result = categories.indexOfFirst { category ->
                        category.id == categoryViewModel.selectedCategoryId.value
                    }
                    if (result != -1) categoryId = result
                }
                val position = categoryId + SPINNER_SKIP
                if (selectedPosition != position) {
                    binding.spinnerCategory.setSelection(position)
                }
            }
        }
    }

    private fun showMenu(anchor: View) {
        PopupMenu(ContextThemeWrapper(this, R.style.PopupMenu), anchor).apply {
            gravity = Gravity.END
            inflate(R.menu.popup_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.item_task_categories -> {
                        startActivity(Intent(
                            applicationContext,
                            CategoryActivity::class.java))
                        true
                    }
                    R.id.item_settings -> {
                        startActivity(Intent(
                            applicationContext,
                            SettingsActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun deselectTasks() {
        taskSectionManager.deselectTasks()
    }

    private fun updateViewsOnTasksSelected() {
        binding.buttonBack.visibility = View.VISIBLE
        binding.checkboxCompleted.visibility = View.GONE
        binding.spinnerCategory.visibility = View.INVISIBLE
        binding.buttonShare.visibility = View.VISIBLE
        binding.buttonDelete.visibility = View.VISIBLE
    }

    private fun updateViewsOnTasksDeselected() {
        binding.buttonBack.visibility = View.GONE
        binding.checkboxCompleted.visibility = View.VISIBLE
        binding.spinnerCategory.visibility = View.VISIBLE
        binding.buttonShare.visibility = View.GONE
        binding.buttonDelete.visibility = View.GONE
    }

    private fun setupDialogListeners() {
        // Add Category
        supportFragmentManager.setFragmentResultListener(
            AddCategoryDialog.REQUEST_KEY,
            this,
            { _, bundle ->
                val name = bundle.getString(AddCategoryDialog.RESULT_KEY) ?:
                return@setFragmentResultListener
                categoryViewModel.addCategory(Category(name = name))
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
                    selectedTasksViewModel.selectedTasks.value?.forEach { task ->
                        taskViewModel.deleteTask(task)
                    }
                    deselectTasks()
                }
            }
        )
    }
}