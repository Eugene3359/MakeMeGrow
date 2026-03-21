package com.scipath.makemegrow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    private var selectedTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView: RecyclerView = findViewById(R.id.view_tasks)
        val buttonDeleteTask: Button = findViewById(R.id.button_delete_task)
        val buttonNewTask: Button = findViewById(R.id.button_new_task)

        val dao = AppDatabase.getDatabase(applicationContext).taskDao()
        val repository = TaskRepository(dao)
        val factory = TaskViewModelFactory(repository)
        val taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        val adapter = TaskAdapter(
            emptyList(),
            taskViewModel,
            onTaskClick = { task ->
                val intent = Intent(this, TaskActivity::class.java)
                intent.putExtra("task", task)
                startActivity(intent)
            },
            onTaskSelect = { task ->
                selectedTask = task
                buttonDeleteTask.visibility =
                    if (task == null) View.GONE else View.VISIBLE
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        taskViewModel.tasks.observe(this) { tasks ->
            adapter.updateTasks(tasks)
        }

        buttonDeleteTask.setOnClickListener {
            selectedTask?.let {
                taskViewModel.deleteTask(it)
                selectedTask = null
                buttonDeleteTask.visibility = View.GONE
                adapter.clearSelection()
            }
        }

        buttonNewTask.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            startActivity(intent)
        }
    }
}