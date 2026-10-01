package com.scipath.makemegrow.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.scipath.makemegrow.R

class TaskNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "task_deadlines"
    }

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notifications),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notifications)
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}