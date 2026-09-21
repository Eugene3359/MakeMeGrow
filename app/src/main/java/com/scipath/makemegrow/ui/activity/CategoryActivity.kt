package com.scipath.makemegrow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.common.CategoryIds.DEFAULT
import com.scipath.makemegrow.data.converter.TaskShareConverter.toShareString
import com.scipath.makemegrow.data.model.Category
import com.scipath.makemegrow.databinding.ActivityCategoryBinding
import com.scipath.makemegrow.ui.adapter.CategoryAdapter
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.dialog.DeleteCategoriesDialog
import com.scipath.makemegrow.ui.dialog.DeleteCategoryDialog
import com.scipath.makemegrow.ui.dialog.EditCategoryDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel
import com.scipath.makemegrow.ui.viewmodel.TaskViewModel

class CategoryActivity : AppCompatActivity() {

    private lateinit var categoryViewModel: CategoryViewModel
    private var pendingCategory: Category? = null
    private lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as MakeMeGrowApp
        val taskViewModel = ViewModelProvider(this, app.taskFactory)[TaskViewModel::class.java].apply {
            allTasks.observe(this@CategoryActivity) {}
            overdueTasks.observe(this@CategoryActivity) {}
        }
        categoryViewModel = ViewModelProvider(this, app.categoryFactory)[CategoryViewModel::class.java].apply {
            selectedCategoryIds.observe(this@CategoryActivity) { categoryIds ->
                if (categoryIds.isEmpty()) {
                    binding.buttonShare.visibility = View.GONE
                    binding.buttonDelete.visibility = View.GONE
                } else {
                    binding.buttonShare.visibility = View.VISIBLE
                    if (null in categoryIds) {
                        binding.buttonDelete.visibility = View.GONE
                    } else {
                        binding.buttonDelete.visibility = View.VISIBLE
                    }
                }
            }
        }
        val settingsViewModel = ViewModelProvider(this, app.settingsFactory)[SettingsViewModel::class.java].apply {
            timeFormat24.observe(this@CategoryActivity) {}
        }

        // Categories
        binding.viewCategories.layoutManager = LinearLayoutManager(this)
        val adapter = CategoryAdapter(
            emptyList(),
            taskViewModel,
            onEdit = { category ->
                pendingCategory = category
                EditCategoryDialog
                    .newInstance(category)
                    .show(supportFragmentManager, "EditCategoryDialog")
            },
            onDelete = { category ->
                pendingCategory = category
                DeleteCategoryDialog().show(supportFragmentManager, "DeleteCategoryDialog")
            },
            onCategoryClick = { category ->
                categoryViewModel.selectCategory(category?.id ?: DEFAULT)
                finish()
            },
            onCategoryLongClick = { category, isSelected ->
                if (isSelected) {
                    categoryViewModel.addSelectedCategory(category?.id)
                } else {
                    categoryViewModel.removeSelectedCategory(category?.id)
                }
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

        // Taskbar Elements
        // Button Share Category
        binding.buttonShare.setOnClickListener {
            val text = taskViewModel.allTasks.value?.filter {
                categoryViewModel.selectedCategoryIds.value?.contains(it.categoryId) == true
            }?.toShareString(settingsViewModel.isTimeFormat24(), this)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        // Button Delete Category
        binding.buttonDelete.setOnClickListener {
            DeleteCategoriesDialog().show(supportFragmentManager, "DeleteCategoriesDialog")
        }

        // Button Back
        binding.buttonBack.setOnClickListener {
            finish()
        }

        setupDialogListeners()
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

        // Rename Category
        supportFragmentManager.setFragmentResultListener(
            EditCategoryDialog.REQUEST_KEY,
            this,
            { _, bundle ->
                val category = bundle.getSerializable(
                    EditCategoryDialog.RESULT_KEY
                ) as Category
                categoryViewModel.updateCategory(category)
            }
        )

        // Delete Category
        supportFragmentManager.setFragmentResultListener(
            DeleteCategoryDialog.REQUEST_KEY,
            this,
            { _, bundle ->
                val isConfirmed = bundle.getBoolean(DeleteCategoryDialog.RESULT_KEY)
                if (isConfirmed) {
                    pendingCategory?.let {
                        categoryViewModel.deleteCategory(it)
                    }
                }
                pendingCategory = null
            }
        )

        // Delete Categories
        supportFragmentManager.setFragmentResultListener(
            DeleteCategoriesDialog.REQUEST_KEY,
            this,
            { _, bundle ->
                val isConfirmed = bundle.getBoolean(DeleteCategoriesDialog.RESULT_KEY)
                if (isConfirmed) {
                    categoryViewModel.deleteSelectedCategories()
                }
            }
        )
    }
}