package com.scipath.makemegrow.app

import android.app.Application
import com.scipath.makemegrow.data.local.AppDatabase
import com.scipath.makemegrow.data.repository.SettingsRepository
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModelFactory
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModelFactory
import com.scipath.makemegrow.ui.viewmodel.TaskViewModelFactory
import kotlin.getValue

class MakeMeGrowApp : Application() {

    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    val taskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    val categoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val settingsRepository by lazy {
        SettingsRepository(this)
    }

    val taskFactory by lazy {
        TaskViewModelFactory(taskRepository)
    }

    val categoryFactory by lazy {
        CategoryViewModelFactory(categoryRepository)
    }

    val settingsFactory by lazy {
        SettingsViewModelFactory(settingsRepository)
    }
}