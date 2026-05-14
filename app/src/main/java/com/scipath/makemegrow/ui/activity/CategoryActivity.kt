package com.scipath.makemegrow.ui.activity

import android.os.Bundle
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
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.ui.adapter.CategoryAdapter
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.dialog.RenameCategoryDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModelFactory
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModelFactory

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val categoryDao = AppDatabase.getDatabase(applicationContext).categoryDao()
        val categoryRepository = CategoryRepository(categoryDao)
        val categoryFactory = CategoryViewModelFactory(categoryRepository)
        val categoryViewModel = ViewModelProvider(this, categoryFactory)[CategoryViewModel::class.java]

        val taskDao = AppDatabase.getDatabase(applicationContext).taskDao()
        val taskRepository = TaskRepository(taskDao)
        val taskFactory = TaskViewModelFactory(taskRepository)
        val taskViewModel = ViewModelProvider(this, taskFactory)[TaskViewModel::class.java]
        taskViewModel.allTasks.observe(this) { return@observe }
        taskViewModel.overdueTasks.observe(this) { return@observe }

        val viewCategories: RecyclerView = findViewById(R.id.view_categories)
        viewCategories.layoutManager = LinearLayoutManager(this)
        val adapter = CategoryAdapter(
            emptyList(),
            taskViewModel,
            onEdit = { category ->
                RenameCategoryDialog().show(supportFragmentManager, "RenameCategoryDialog")
                supportFragmentManager.setFragmentResultListener(
                    RenameCategoryDialog.REQUEST_KEY,
                    this
                ) { _, bundle ->
                    val name = bundle.getString(RenameCategoryDialog.RESULT_KEY) ?:
                    return@setFragmentResultListener
                    category.name = name
                    categoryViewModel.updateCategory(category)
                }
            },
            onDelete = { }
        )
        viewCategories.adapter = adapter

        categoryViewModel.allCategories.observe(this) { categories ->
            adapter.updateCategories(categories)
        }

        // Button New Category
        val buttonNewCategory: Button = findViewById(R.id.button_new_category)
        buttonNewCategory.setOnClickListener {
            AddCategoryDialog().show(supportFragmentManager, "AddCategoryDialog")
        }

        // Add Category
        supportFragmentManager.setFragmentResultListener(
            AddCategoryDialog.REQUEST_KEY,
            this
        ) { _, bundle ->
            val name = bundle.getString(AddCategoryDialog.RESULT_KEY)
            name?.let { categoryViewModel.addCategory(Category(name = it), null) }
        }

        // Button Back
        val buttonBack: Button = findViewById(R.id.button_back)
        buttonBack.setOnClickListener {
            finish()
        }
    }
}