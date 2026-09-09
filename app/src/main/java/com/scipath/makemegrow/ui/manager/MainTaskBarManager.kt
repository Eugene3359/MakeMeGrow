package com.scipath.makemegrow.ui.manager

import android.content.Intent
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.common.CategoryIds.ALL
import com.scipath.makemegrow.data.common.CategoryIds.DEFAULT
import com.scipath.makemegrow.data.converter.TaskToStringConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.databinding.ActivityMainBinding
import com.scipath.makemegrow.ui.activity.CategoryActivity
import com.scipath.makemegrow.ui.activity.SettingsActivity
import com.scipath.makemegrow.ui.adapter.CategoryArrayAdapter
import com.scipath.makemegrow.ui.dialog.AddCategoryDialog
import com.scipath.makemegrow.ui.dialog.DeleteTasksDialog
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModel
import com.scipath.makemegrow.ui.viewmodel.SelectedTasksViewModel
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModel

class MainTaskBarManager(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
    private val selectedTasksViewModel: SelectedTasksViewModel,
    private val categoryViewModel: CategoryViewModel,
    private val settingsViewModel: SettingsViewModel,
    private val onCompletionFilterChange: (isChecked: Boolean) -> Unit,
    private val deselectTasks: () -> Unit
) {

    companion object {
        private const val SPINNER_SKIP = 2
    }

    private lateinit var categoryAdapter: CategoryArrayAdapter
    private var categoryNames: List<String> = mutableListOf()

    fun setupTaskbar() {
        binding.apply {
            buttonBack.setOnClickListener {
                deselectTasks()
            }

            checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                onCompletionFilterChange(isChecked)
            }

            setupCategorySpinner()

            buttonShare.setOnClickListener {
                val text = selectedTasksViewModel.selectedTasks.value
                    ?.sortedWith(
                        compareBy<Task> { it.deadlineDate }
                            .thenBy { it.deadlineTime }
                            .thenBy { it.name }
                    )
                    ?.joinToString(separator = "") { task ->
                        TaskToStringConverter.convert(
                            task,
                            settingsViewModel.isTimeFormat24(),
                            activity
                        )
                    }

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                activity.startActivity(shareIntent)
                deselectTasks()
            }

            buttonDelete.setOnClickListener {
                DeleteTasksDialog().show(activity.supportFragmentManager, "DeleteTasksDialog")
            }

            buttonMenu.setOnClickListener {
                showMenu(it)
            }
        }
    }

    private fun setupCategorySpinner() {
        categoryViewModel.allCategories.observe(activity) { categories ->
            categoryNames = buildList {
                add(activity.getString(R.string.all_tasks))
                add(activity.getString(R.string.default_category))
                addAll(categories.map { it.name })
                add(activity.getString(R.string.add_category))
            }

            categoryAdapter.clear()
            categoryAdapter.addAll(categoryNames)
            categoryAdapter.notifyDataSetChanged()
            updateCategorySpinner()
        }

        categoryAdapter = CategoryArrayAdapter(activity, mutableListOf())
        binding.spinnerCategory.adapter = categoryAdapter
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == categoryNames.lastIndex) {
                    // Add Category
                    updateCategorySpinner()
                    AddCategoryDialog().show(activity.supportFragmentManager, "AddCategoryDialog")
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

    fun updateCategorySpinner() {
        val selectedPosition = binding.spinnerCategory.selectedItemPosition
        when (categoryViewModel.selectedCategoryId.value) {
            ALL -> {
                if (selectedPosition != ALL + SPINNER_SKIP)
                    binding.spinnerCategory.setSelection(0)
            }
            DEFAULT -> {
                if (selectedPosition != DEFAULT + SPINNER_SKIP)
                    binding.spinnerCategory.setSelection(1)
            }
            else -> {
                val categoryIndex: Int = categoryViewModel.allCategories.value
                    ?.indexOfFirst { it.id == categoryViewModel.selectedCategoryId.value }
                    ?.apply { if (this == -1) ALL }
                    ?: ALL
                val newPosition = categoryIndex + SPINNER_SKIP
                if (selectedPosition != newPosition) {
                    binding.spinnerCategory.setSelection(newPosition)
                }
            }
        }
    }

    private fun showMenu(anchor: View) {
        PopupMenu(ContextThemeWrapper(activity, R.style.PopupMenu), anchor).apply {
            gravity = Gravity.END
            inflate(R.menu.popup_menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.item_task_categories -> {
                        activity.startActivity(Intent(
                            activity.applicationContext,
                            CategoryActivity::class.java))
                        true
                    }
                    R.id.item_settings -> {
                        activity.startActivity(Intent(
                            activity.applicationContext,
                            SettingsActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    fun updateVisibilities(isSelected: Boolean) {
        if (isSelected) {
            binding.buttonBack.visibility = View.VISIBLE
            binding.checkboxCompleted.visibility = View.GONE
            binding.spinnerCategory.visibility = View.INVISIBLE
            binding.buttonShare.visibility = View.VISIBLE
            binding.buttonDelete.visibility = View.VISIBLE
        } else {
            binding.buttonBack.visibility = View.GONE
            binding.checkboxCompleted.visibility = View.VISIBLE
            binding.spinnerCategory.visibility = View.VISIBLE
            binding.buttonShare.visibility = View.GONE
            binding.buttonDelete.visibility = View.GONE
        }
    }
}