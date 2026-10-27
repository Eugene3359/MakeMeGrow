package com.scipath.makemegrow.app

import android.app.Application
import com.scipath.makemegrow.data.local.AppDatabase
import com.scipath.makemegrow.data.notification.NotificationScheduler
import com.scipath.makemegrow.data.notification.TaskNotificationManager
import com.scipath.makemegrow.data.repository.SettingsRepository
import com.scipath.makemegrow.data.repository.CategoryRepository
import com.scipath.makemegrow.data.repository.TaskRepository
import com.scipath.makemegrow.ui.viewmodel.CategoryViewModelFactory
import com.scipath.makemegrow.ui.viewmodel.SettingsViewModelFactory
import com.scipath.makemegrow.ui.viewmodel.TaskViewModelFactory
import kotlin.getValue

class MakeMeGrowApp : Application() {

    lateinit var notificationManager: TaskNotificationManager
        private set

    lateinit var notificationScheduler: NotificationScheduler
        private set

    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    val settingsRepository by lazy {
        SettingsRepository(this)
    }

    val taskRepository by lazy {
        TaskRepository(database.taskDao(), settingsRepository, notificationScheduler)
    }

    val categoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val settingsFactory by lazy {
        SettingsViewModelFactory(taskRepository, settingsRepository)
    }

    val taskFactory by lazy {
        TaskViewModelFactory(taskRepository)
    }

    val categoryFactory by lazy {
        CategoryViewModelFactory(categoryRepository)
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = TaskNotificationManager(this)
        notificationScheduler = NotificationScheduler(this)
        notificationManager.createChannel()
    }
}