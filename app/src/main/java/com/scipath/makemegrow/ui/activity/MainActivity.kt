package com.scipath.makemegrow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.R
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.common.CategoryIds.ALL
import com.scipath.makemegrow.data.common.CategoryIds.DEFAULT
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.model.Task.RepeatType.NO_REPEAT
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_DAY
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_MONTH
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_WEEK
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_YEAR
import com.scipath.makemegrow.data.model.Task.RepeatType.ON_WEEKDAYS
import com.scipath.makemegrow.data.model.Task.RepeatType.ON_WEEKENDS
import com.scipath.makemegrow.databinding.ActivityMainBinding
import com.scipath.makemegrow.ui.adapter.CategoryArrayAdapter
import com.scipath.makemegrow.ui.adapter.TaskAdapter
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.dialog.DeleteTasksDialog
import com.scipath.makemegrow.ui.dialog.TaskCompletionDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEV_MODE = true
        private const val SPINNER_SKIP = 2
    }

    data class TaskSection(
        val liveData: LiveData<List<Task>>,
        val parentLayout: LinearLayout,
        val recyclerView: RecyclerView,
        var adapter: TaskAdapter?
    )

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private var selectedTasks: MutableList<Task> = mutableListOf()
    private var pendingTask: Task? = null
    private var onTaskCompletionCancel: (() -> Unit)? = null
    private var categoryNames: List<String> = mutableListOf()
    private lateinit var binding: ActivityMainBinding
    private val taskSections: MutableList<TaskSection> = mutableListOf()
    private lateinit var categoryAdapter: CategoryArrayAdapter
    private var displayCompletedTasks: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as MakeMeGrowApp
        taskViewModel = ViewModelProvider(this, app.taskFactory)[TaskViewModel::class.java]
        categoryViewModel = ViewModelProvider(this, app.categoryFactory)[CategoryViewModel::class.java]
        settingsViewModel = ViewModelProvider(this, app.settingsFactory)[SettingsViewModel::class.java]
        settingsViewModel.confirmationOfCompletion.observe(this) {}
        settingsViewModel.timeFormat24.observe(this) {}

        // Seed Database
        if (DEV_MODE && savedInstanceState == null) {
            categoryViewModel.seedDatabase()
            taskViewModel.seedDatabase()
        }

        setupTaskbar()
        setupAllTaskSections()

        binding.buttonNewTask.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }
    }

    private fun setupTaskbar() {
        binding.checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
            displayCompletedTasks = isChecked
            taskSections.forEach(::updateTaskSection)
        }

        setupCategorySpinner()

        binding.buttonDelete.setOnClickListener {
            DeleteTasksDialog().show(supportFragmentManager, "DeleteTasksDialog")
        }

        // Button Menu
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
            taskSections.forEach(::updateTaskSection)
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

    private fun setupAllTaskSections() {
        taskSections.clear()

        // Overdue
        taskSections.add(TaskSection(
            taskViewModel.overdueTasks,
            binding.layoutOverdueTasks,
            binding.viewOverdueTasks,
            null
        ))

        // Today
        taskSections.add(TaskSection(
            taskViewModel.todayTasks,
            binding.layoutTodayTasks,
            binding.viewTodayTasks,
            null
        ))

        // Tomorrow
        taskSections.add(TaskSection(
            taskViewModel.tomorrowTasks,
            binding.layoutTomorrowTasks,
            binding.viewTomorrowTasks,
                null
        ))

        // This Week
        taskSections.add(TaskSection(
            taskViewModel.thisWeekTasks,
            binding.layoutThisWeekTasks,
            binding.viewThisWeekTasks,
            null
        ))

        // Next Week
        taskSections.add(TaskSection(
            taskViewModel.nextWeekTasks,
            binding.layoutNextWeekTasks,
            binding.viewNextWeekTasks,
            null
        ))

        // This Month
        taskSections.add(TaskSection(
            taskViewModel.thisMonthTasks,
            binding.layoutThisMonthTasks,
            binding.viewThisMonthTasks,
            null
        ))

        // Next Month
        taskSections.add(TaskSection(
            taskViewModel.nextMonthTasks,
            binding.layoutNextMonthTasks,
            binding.viewNextMonthTasks,
            null
        ))

        // Later
        taskSections.add(TaskSection(
            taskViewModel.laterTasks,
            binding.layoutLaterTasks,
            binding.viewLaterTasks,
            null
        ))

        // Completed
        taskSections.add(TaskSection(
            taskViewModel.completedTasks,
            binding.layoutCompletedTasks,
            binding.viewCompletedTasks,
            null
        ))

        taskSections.forEach { taskSection ->
            setupTaskSectionAdapter(taskSection)
            taskSection.liveData.observe(this) {
                updateTaskSection(taskSection)
            }
        }

        settingsViewModel.timeFormat24.observe(this) { timeFormat24 ->
            taskSections.forEach { taskSection ->
                taskSection.adapter?.updateTimeFormat(timeFormat24)
            }
        }
    }

    private fun setupTaskSectionAdapter(taskSection: TaskSection) {
        val adapter = TaskAdapter(
            tasks = emptyList(),
            onTaskClick = { task ->
                val intent = Intent(this, TaskActivity::class.java)
                intent.putExtra("task", task)
                startActivity(intent)
            },
            onTaskSelect = { task, isSelected ->
                if (isSelected) {
                    selectedTasks.add(task)
                } else {
                    selectedTasks.remove(task)
                }
                binding.buttonDelete.visibility =
                    if (selectedTasks.isEmpty()) View.GONE else View.VISIBLE
            },
            onTaskChecked = { task, isChecked, onCancel ->
                if (settingsViewModel.isConfirmationOfCompletionEnabled() && isChecked) {
                    pendingTask = task
                    onTaskCompletionCancel = onCancel
                    TaskCompletionDialog().show(supportFragmentManager, "TaskCompletionDialog")
                } else {
                    checkTask(task, isChecked)
                }
            }
        )

        taskSection.adapter = adapter
        taskSection.recyclerView.layoutManager = LinearLayoutManager(this)
        taskSection.recyclerView.adapter = adapter
        taskSection.recyclerView.isNestedScrollingEnabled = false
    }

    private fun checkTask(task: Task, isChecked: Boolean) {
        val deadlineDate: LocalDate? = DateAndTimeConverter.secondsToDate(task.deadlineDate)
        when (task.repeat) {
            NO_REPEAT -> task.isCompleted = isChecked
            ONCE_A_DAY -> {
                deadlineDate?.let {
                    task.deadlineDate = DateAndTimeConverter.dateToSeconds(
                        it.plusDays(1)
                    )
                }
            }
            ON_WEEKDAYS -> {
                deadlineDate?.let {
                    task.deadlineDate = DateAndTimeConverter.dateToSeconds(
                        it.plusDays(
                            if (it.dayOfWeek.value < 5) 1
                            else 8L - it.dayOfWeek.value
                        )
                    )
                }
            }
            ON_WEEKENDS -> {
                deadlineDate?.let {
                    task.deadlineDate = DateAndTimeConverter.dateToSeconds(
                        it.plusDays(
                            if (it.dayOfWeek.value == 6) 1
                            else if (it.dayOfWeek.value == 7) 6
                            else 6L - it.dayOfWeek.value
                        )
                    )
                }
            }
            ONCE_A_WEEK -> {
                deadlineDate?.let {
                    task.deadlineDate = DateAndTimeConverter.dateToSeconds(
                        it.plusWeeks(1)
                    )
                }
            }
            ONCE_A_MONTH -> {
                deadlineDate?.let {
                    task.deadlineDate = DateAndTimeConverter.dateToSeconds(
                        it.plusMonths(1)
                    )
                }
            }
            ONCE_A_YEAR -> {
                deadlineDate?.let {
                    task.deadlineDate = DateAndTimeConverter.dateToSeconds(
                        it.plusYears(1)
                    )
                }
            }
        }
        taskViewModel.updateTask(task)
    }

    private fun updateTaskSection(taskSection: TaskSection) {
        val filteredTasks = filterTasks(taskSection.liveData)
        taskSection.parentLayout.visibility =
            if (filteredTasks.isEmpty() ||
                filteredTasks.first().isCompleted != displayCompletedTasks)
                View.GONE
            else View.VISIBLE
        taskSection.adapter?.updateTasks(filteredTasks)
    }

    private fun filterTasks(tasks: LiveData<List<Task>>) : List<Task> {
        return when (categoryViewModel.selectedCategoryId.value) {
            ALL -> tasks.value ?: emptyList()
            DEFAULT -> taskViewModel.filterTasksByCategory(tasks, null)
            else -> taskViewModel.filterTasksByCategory(
                tasks,
                categoryViewModel.allCategories.value?.find { it.id == categoryViewModel.selectedCategoryId.value }
            )
        }
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
                        checkTask(task, true)
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
                    selectedTasks.forEach { task ->
                        taskViewModel.deleteTask(task)
                    }
                    selectedTasks.clear()
                    binding.buttonDelete.visibility = View.GONE
                }
            }
        )
    }
}