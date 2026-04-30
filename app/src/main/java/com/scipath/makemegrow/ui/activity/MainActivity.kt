package com.scipath.makemegrow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.local.AppDatabase
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.TaskCategory
import com.scipath.makemegrow.data.repository.TaskCategoryRepository
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.ui.adapter.TaskAdapter
import com.scipath.makemegrow.ui.viewmodel.TaskCategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskCategoryViewModelFactory
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModelFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEV_MODE = true
    }

    data class TaskSection(
        val liveData: LiveData<List<Task>>,
        val parentLayout: LinearLayout,
        val recyclerView: RecyclerView,
        var adapter: TaskAdapter?
    )

    private lateinit var taskViewModel: TaskViewModel
    private val taskSections: MutableList<TaskSection> = mutableListOf()
    private var selectedTasks: MutableList<Task> = mutableListOf()
    private var isCompleted: Boolean = false
    private var selectedCategoryPosition: Int = -2
    private var selectedCategory: TaskCategory? = null
    private lateinit var buttonDeleteTask: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val taskDao = AppDatabase.getDatabase(applicationContext).taskDao()
        val taskRepository = TaskRepository(taskDao)
        val taskFactory = TaskViewModelFactory(taskRepository)
        taskViewModel = ViewModelProvider(this, taskFactory)[TaskViewModel::class.java]
        val categoryDao = AppDatabase.getDatabase(applicationContext).taskCategoryDao()
        val categoryRepository = TaskCategoryRepository(categoryDao)
        val categoryFactory = TaskCategoryViewModelFactory(categoryRepository)
        val categoryViewModel = ViewModelProvider(this, categoryFactory)[TaskCategoryViewModel::class.java]

        if (DEV_MODE) {
            categoryViewModel.seedDatabase()
            taskViewModel.seedDatabase()
        }

        setupAllRecycleViews()

        // Checkbox completed
        val checkboxCompleted: CheckBox = findViewById(R.id.checkbox_completed)
        checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
            isCompleted = isChecked
            taskSections.forEach {
                updateViews(it)
            }
        }

        // Category selection
        val spinnerCategory: Spinner = findViewById(R.id.spinner_category)
        categoryViewModel.allTaskCategories.observe(this) { categories ->
            val categoryNames = listOf(
                getString(R.string.all_tasks),
                getString(R.string.default_category)
            ) + categories.map { it.name } + getString(R.string.add_category)

            spinnerCategory.adapter = ArrayAdapter(
                this,
                R.layout.layout_item_large,
                categoryNames
            )

            spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    if (position == categoryNames.lastIndex) {
                        // Add Category
                        spinnerCategory.setSelection(selectedCategoryPosition + 2)
                        startAddCategoryDialogue()
                    } else {
                        // Change Category
                        selectedCategoryPosition = position - 2
                        selectedCategory =
                            if (selectedCategoryPosition >= 0) categories[selectedCategoryPosition]
                            else null
                        taskSections.forEach {
                            updateViews(it)
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        val buttonNewTask: Button = findViewById(R.id.button_new_task)
        buttonNewTask.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }

        buttonDeleteTask = findViewById(R.id.button_delete)
        buttonDeleteTask.setOnClickListener {
            selectedTasks.forEach { task ->
                taskViewModel.deleteTask(task)
            }
            selectedTasks.clear()
            buttonDeleteTask.visibility = View.GONE
            taskSections.forEach {
                it.adapter?.clearSelection()
            }
        }

    }

    private fun setupAllRecycleViews() {
        // Overdue
        taskSections.add(TaskSection(
            taskViewModel.overdueTasks,
            findViewById(R.id.layout_overdue_tasks),
            findViewById(R.id.view_overdue_tasks),
            null
        ))

        // Today
        taskSections.add(TaskSection(
            taskViewModel.todayTasks,
            findViewById(R.id.layout_today_tasks),
            findViewById(R.id.view_today_tasks),
            null
        ))

        // Tomorrow
        taskSections.add(TaskSection(
            taskViewModel.tomorrowTasks,
            findViewById(R.id.layout_tomorrow_tasks),
                findViewById(R.id.view_tomorrow_tasks),
                null
        ))

        // This Week
        taskSections.add(TaskSection(
            taskViewModel.thisWeekTasks,
            findViewById(R.id.layout_this_week_tasks),
            findViewById(R.id.view_this_week_tasks),
            null
        ))

        // Next Week
        taskSections.add(TaskSection(
            taskViewModel.nextWeekTasks,
            findViewById(R.id.layout_next_week_tasks),
            findViewById(R.id.view_next_week_tasks),
            null
        ))

        // This Month
        taskSections.add(TaskSection(
            taskViewModel.thisMonthTasks,
            findViewById(R.id.layout_this_month_tasks),
            findViewById(R.id.view_this_month_tasks),
            null
        ))

        // Next Month
        taskSections.add(TaskSection(
            taskViewModel.nextMonthTasks,
            findViewById(R.id.layout_next_month_tasks),
            findViewById(R.id.view_next_month_tasks),
            null
        ))

        // Later
        taskSections.add(TaskSection(
            taskViewModel.laterTasks,
            findViewById(R.id.layout_later_tasks),
            findViewById(R.id.view_later_tasks),
            null
        ))

        // Completed
        taskSections.add(TaskSection(
            taskViewModel.completedTasks,
            findViewById(R.id.layout_completed_tasks),
            findViewById(R.id.view_completed_tasks),
            null
        ))

        taskSections.forEach {
            setupRecycleView(it)
        }
    }

    private fun setupRecycleView(taskSection: TaskSection)
    {
        val adapter = TaskAdapter(
            emptyList(),
            taskViewModel,
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
                buttonDeleteTask.visibility =
                    if (selectedTasks.isEmpty()) View.GONE else View.VISIBLE
            }
        )

        taskSection.adapter = adapter
        taskSection.recyclerView.layoutManager = LinearLayoutManager(this)
        taskSection.recyclerView.adapter = adapter
        taskSection.recyclerView.isNestedScrollingEnabled = false

        taskSection.liveData.observe(this) {
            updateViews(taskSection)
        }
    }

    private fun updateViews(taskSelection: TaskSection
    ) {
        val filteredTasks = filterTasks(taskSelection.liveData.value ?: listOf())
        taskSelection.parentLayout.visibility =
            if (filteredTasks.isEmpty() ||
                filteredTasks.first().isCompleted != isCompleted)
                View.GONE
            else View.VISIBLE
        taskSelection.adapter?.updateTasks(filteredTasks)
    }

    private fun filterTasks(tasks: List<Task>) : List<Task> {
        return when (selectedCategoryPosition) {
            -2 -> tasks
            -1 -> tasks.filter { it.categoryId == null }
            else -> tasks.filter { it.categoryId == selectedCategory?.id }
        }
    }

    private fun startAddCategoryDialogue() {
        // TODO: Implement
    }
}