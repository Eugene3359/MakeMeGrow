package com.scipath.makemegrow.ui.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.common.CategoryIds.DEFAULT
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.databinding.ActivityCategoryBinding
import com.scipath.makemegrow.ui.adapter.CategoryAdapter
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.dialog.DeleteCategoriesDialog
import com.scipath.makemegrow.ui.dialog.DeleteCategoryDialog
import com.scipath.makemegrow.ui.dialog.RenameCategoryDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel

class CategoryActivity : AppCompatActivity() {

    private var selectedCategories: MutableList<Category> = mutableListOf()
    private lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as MakeMeGrowApp
        val taskViewModel = ViewModelProvider(this, app.taskFactory)[TaskViewModel::class.java]
        val categoryViewModel = ViewModelProvider(this, app.categoryFactory)[CategoryViewModel::class.java]
        taskViewModel.allTasks.observe(this) { return@observe }
        taskViewModel.overdueTasks.observe(this) { return@observe }

        // Categories
        binding.viewCategories.layoutManager = LinearLayoutManager(this)
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
                categoryViewModel.selectCategory(category?.id ?: DEFAULT)
                finish()
            },
            onCategorySelect = { category, isSelected ->
                if (isSelected) {
                    selectedCategories.add(category)
                } else {
                    selectedCategories.remove(category)
                }
                binding.buttonDelete.visibility =
                    if (selectedCategories.isEmpty()) View.GONE else View.VISIBLE
            }
        )
        binding.viewCategories.adapter = adapter

        categoryViewModel.allCategories.observe(this) { categories ->
            adapter.updateCategories(buildList {
                add(null) // Default Category
                addAll(categories)
            })
        }

        // Button New Category
        binding.buttonNewCategory.setOnClickListener {
            AddCategoryDialog().show(supportFragmentManager, "AddCategoryDialog")
        }

        // Add Category
        supportFragmentManager.setFragmentResultListener(
            AddCategoryDialog.REQUEST_KEY,
            this,
            { _, bundle ->
                val name = bundle.getString(AddCategoryDialog.RESULT_KEY)
                name?.let { categoryViewModel.addCategory(Category(name = it)) }
            }
        )

        // Taskbar Elements
        // Button Delete Category
        binding.buttonDelete.setOnClickListener {
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
                    binding.buttonDelete.visibility = View.GONE
                }
            }
        )

        // Button Back
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}