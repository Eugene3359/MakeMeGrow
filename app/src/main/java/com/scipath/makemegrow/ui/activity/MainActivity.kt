package com.scipath.makemegrow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scipath.makemegrow.R
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.databinding.ActivityMainBinding
import com.scipath.makemegrow.ui.adapter.CategoryArrayAdapter
import com.scipath.makemegrow.ui.adapter.TaskAdapter
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.dialog.DeleteTasksDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEV_MODE = true
        private const val ALL_CATEGORIES_ID = -2
        private const val DEFAULT_CATEGORY_ID = -1
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
    private val taskSections: MutableList<TaskSection> = mutableListOf()
    private var selectedTasks: MutableList<Task> = mutableListOf()
    private var categoriesList: List<Category> = mutableListOf()
    private var categoryNames: List<String> = mutableListOf()
    private var selectedCategoryId: Int = ALL_CATEGORIES_ID
    private var isCompleted: Boolean = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val app = application as MakeMeGrowApp
        taskViewModel = ViewModelProvider(this, app.taskFactory)[TaskViewModel::class.java]
        categoryViewModel = ViewModelProvider(this, app.categoryFactory)[CategoryViewModel::class.java]

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
            isCompleted = isChecked
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
            categoriesList = categories

            categoryNames = buildList {
                add(getString(R.string.all_tasks))
                add(getString(R.string.default_category))
                addAll(categories.map { it.name })
                add(getString(R.string.add_category))
            }

            categoryAdapter.clear()
            categoryAdapter.addAll(categoryNames)
            categoryAdapter.notifyDataSetChanged()

            if (selectedCategoryId != DEFAULT_CATEGORY_ID &&
                categories.none { it.id == selectedCategoryId}){
                selectedCategoryId = ALL_CATEGORIES_ID
            }

            updateCategorySpinner()
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
                    categoryAdapter.selectedPosition = position
                    categoryAdapter.notifyDataSetChanged()
                    selectedCategoryId = when (position) {
                        0 -> ALL_CATEGORIES_ID
                        1 -> DEFAULT_CATEGORY_ID
                        else -> categoriesList[position - SPINNER_SKIP].id
                    }
                    taskSections.forEach(::updateTaskSection)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateCategorySpinner() {
        when (selectedCategoryId) {
            ALL_CATEGORIES_ID -> {
                binding.spinnerCategory.setSelection(0)
            }
            DEFAULT_CATEGORY_ID -> {
                binding.spinnerCategory.setSelection(1)
            }
            else -> {
                val categoryIndex: Int = categoriesList.indexOfFirst { it.id == selectedCategoryId }
                binding.spinnerCategory.setSelection(categoryIndex + SPINNER_SKIP)
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
                        categoryActivityLauncher.launch(Intent(
                            applicationContext,
                            CategoryActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private val categoryActivityLauncher = registerForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        callback = { result ->
            if (result.resultCode == RESULT_OK) {
                selectedCategoryId = result.data?.getIntExtra(
                    "selected_category_id",
                    ALL_CATEGORIES_ID
                ) ?: selectedCategoryId
                updateCategorySpinner()
            }
        }
    )

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
    }

    private fun setupTaskSectionAdapter(taskSection: TaskSection) {
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
                binding.buttonDelete.visibility =
                    if (selectedTasks.isEmpty()) View.GONE else View.VISIBLE
            }
        )

        taskSection.adapter = adapter
        taskSection.recyclerView.layoutManager = LinearLayoutManager(this)
        taskSection.recyclerView.adapter = adapter
        taskSection.recyclerView.isNestedScrollingEnabled = false
    }

    private fun updateTaskSection(taskSection: TaskSection) {
        val filteredTasks = filterTasks(taskSection.liveData)
        taskSection.parentLayout.visibility =
            if (filteredTasks.isEmpty() ||
                filteredTasks.first().isCompleted != isCompleted)
                View.GONE
            else View.VISIBLE
        taskSection.adapter?.updateTasks(filteredTasks)
    }

    private fun filterTasks(tasks: LiveData<List<Task>>) : List<Task> {
        return when (selectedCategoryId) {
            ALL_CATEGORIES_ID -> tasks.value ?: emptyList()
            DEFAULT_CATEGORY_ID -> taskViewModel.filterTasksByCategory(tasks, null)
            else -> taskViewModel.filterTasksByCategory(
                tasks,
                categoryViewModel.allCategories.value?.find { it.id == selectedCategoryId }
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
                categoryViewModel.addCategory(
                    Category(name = name)
                ) { isSuccessful ->
                    if (!isSuccessful) return@addCategory
                }
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