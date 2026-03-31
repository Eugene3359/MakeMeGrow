package com.scipath.makemegrow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.ui.adapter.TaskAdapter
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModelFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEV_MODE = true
    }

    private lateinit var buttonDeleteTask: Button
    private lateinit var taskViewModel: TaskViewModel
    private val adapters: MutableList<TaskAdapter> = mutableListOf()
    private var selectedTasks: MutableList<Task> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dao = AppDatabase.getDatabase(applicationContext).taskDao()
        val repository = TaskRepository(dao)
        val factory = TaskViewModelFactory(repository)
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]
        if (DEV_MODE) {
            taskViewModel.seedDatabase()
        }

        buttonDeleteTask = findViewById(R.id.button_delete)
        buttonDeleteTask.setOnClickListener {
            selectedTasks.forEach { task ->
                taskViewModel.deleteTask(task)
            }
            selectedTasks.clear()
            buttonDeleteTask.visibility = View.GONE
            adapters.forEach { adapter ->
                adapter.clearSelection()
            }
        }

        val buttonNewTask: Button = findViewById(R.id.button_new_task)
        buttonNewTask.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }

        // Overdue
        val layoutOverdueTasks: LinearLayout = findViewById(R.id.layout_overdue_tasks)
        val overdueTasksRecyclerView: RecyclerView = findViewById(R.id.view_overdue_tasks)
        setupRecycleView(
            taskViewModel.overdueTasks,
            overdueTasksRecyclerView,
            layoutOverdueTasks
        )

        // Today
        val layoutTodayTasks: LinearLayout = findViewById(R.id.layout_today_tasks)
        val todayTasksRecyclerView: RecyclerView = findViewById(R.id.view_today_tasks)
        setupRecycleView(
            taskViewModel.todayTasks,
            todayTasksRecyclerView,
            layoutTodayTasks
        )

        // Tomorrow
        val layoutTomorrowTasks: LinearLayout = findViewById(R.id.layout_tomorrow_tasks)
        val tomorrowTasksRecyclerView: RecyclerView = findViewById(R.id.view_tomorrow_tasks)
        setupRecycleView(
            taskViewModel.tomorrowTasks,
            tomorrowTasksRecyclerView,
            layoutTomorrowTasks
        )

        // This Week
        val layoutThisWeekTasks: LinearLayout = findViewById(R.id.layout_this_week_tasks)
        val thisWeekTasksRecyclerView: RecyclerView = findViewById(R.id.view_this_week_tasks)
        setupRecycleView(
            taskViewModel.thisWeekTasks,
            thisWeekTasksRecyclerView,
            layoutThisWeekTasks
        )

        // Next Week
        val layoutNextWeekTasks: LinearLayout = findViewById(R.id.layout_next_week_tasks)
        val nextWeekTasksRecyclerView: RecyclerView = findViewById(R.id.view_next_week_tasks)
        setupRecycleView(
            taskViewModel.nextWeekTasks,
            nextWeekTasksRecyclerView,
            layoutNextWeekTasks
        )

        // Later
        val layoutLaterTasks: LinearLayout = findViewById(R.id.layout_later_tasks)
        val laterTasksRecyclerView: RecyclerView = findViewById(R.id.view_later_tasks)
        setupRecycleView(
            taskViewModel.laterTasks,
            laterTasksRecyclerView,
            layoutLaterTasks
        )
    }

    private fun setupRecycleView(
        observableData: LiveData<List<Task>>,
        recycleView: RecyclerView,
        parentLayout: LinearLayout
    )
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

        adapters.add(adapter)
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = adapter

        observableData.observe(this) { tasks ->
            if(tasks.isEmpty()) parentLayout.visibility = View.GONE
            else parentLayout.visibility = View.VISIBLE
            adapter.updateTasks(tasks)
        }
    }
}