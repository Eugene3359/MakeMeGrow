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
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.ui.adapter.CategoryAdapter
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.dialog.DeleteCategoriesDialog
import com.scipath.makemegrow.ui.dialog.DeleteCategoryDialog
import com.scipath.makemegrow.ui.dialog.RenameCategoryDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModelFactory
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModelFactory

class CategoryActivity : AppCompatActivity() {

    private var selectedCategories: MutableList<Category> = mutableListOf()
    private lateinit var buttonDeleteCategories: Button

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

        // Categories
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
            onDelete = { category ->
                DeleteCategoryDialog().show(supportFragmentManager, "DeleteCategoryDialog")
                supportFragmentManager.setFragmentResultListener(
                    DeleteCategoryDialog.REQUEST_KEY,
                    this
                ) { _, bundle ->
                    val isConfirmed = bundle.getBoolean(DeleteCategoryDialog.RESULT_KEY)
                    if (isConfirmed) {
                        categoryViewModel.deleteCategory(category)
                    }
                }
            },
            onCategoryClick = { category ->
                val resultIntent = Intent().apply {
                    putExtra("selected_category_id", category.id)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            },
            onCategorySelect = { category, isSelected ->
                if (isSelected) {
                    selectedCategories.add(category)
                } else {
                    selectedCategories.remove(category)
                }
                buttonDeleteCategories.visibility =
                    if (selectedCategories.isEmpty()) View.GONE else View.VISIBLE
            }
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
            this,
            { _, bundle ->
                val name = bundle.getString(AddCategoryDialog.RESULT_KEY)
                name?.let { categoryViewModel.addCategory(Category(name = it), null) }
            }
        )

        // Taskbar Elements
        // Button Delete Category
        buttonDeleteCategories = findViewById(R.id.button_delete)
        buttonDeleteCategories.setOnClickListener {
            DeleteCategoriesDialog().show(supportFragmentManager, "DeleteCategoriesDialog")
        }

        // Delete Categories
        supportFragmentManager.setFragmentResultListener(
            DeleteCategoriesDialog.REQUEST_KEY,
            this,
            { _, bundle ->
                val isConfirmed = bundle.getBoolean(DeleteCategoriesDialog.RESULT_KEY)
                if (isConfirmed) {
                    selectedCategories.forEach { category ->
                        categoryViewModel.deleteCategory(category)
                    }
                    selectedCategories.clear()
                    buttonDeleteCategories.visibility = View.GONE
                }
            }
        )

        // Button Back
        val buttonBack: Button = findViewById(R.id.button_back)
        buttonBack.setOnClickListener {
            finish()
        }
    }
}