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
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModelFactory

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

        val viewCategories: RecyclerView = findViewById(R.id.view_categories)
        viewCategories.layoutManager = LinearLayoutManager(this)
        // viewCategories.adapter = TODO: RecyclerView Adapter

        categoryViewModel.allCategories.observe(this) {
            // TODO: Notify viewCategories.adapter that dataset has changed
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
            val name = bundle.getString(AddCategoryDialog.RESULT_KEY_NAME)
            name?.let { categoryViewModel.addCategory(Category(name = it), null) }
        }

        // Button Back
        val buttonBack: Button = findViewById(R.id.button_back)
        buttonBack.setOnClickListener {
            finish()
        }
    }
}