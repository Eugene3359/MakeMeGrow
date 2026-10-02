package com.scipath.makemegrow.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.scipath.makemegrow.app.MakeMeGrowApp
import com.scipath.makemegrow.data.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "task_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        if (taskId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as MakeMeGrowApp
                val task: Task? = app.taskRepository.getById(taskId)
                val notificationManager = app.notificationManager

                if (task != null && !task.isCompleted) {
                    notificationManager.showTaskNotification(task)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}