package com.scipath.makemegrow.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.scipath.makemegrow.app.MakeMeGrowApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskNotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE = "com.scipath.makemegrow.ACTION_COMPLETE_TASK"
        const val EXTRA_TASK_ID = "task_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_COMPLETE) return

        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        if (taskId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as MakeMeGrowApp
                val repository = app.taskRepository
                val task = repository.getById(taskId)

                if (task != null && !task.isCompleted) {
                    app.taskRepository.completeTask(task, true)
                }
                NotificationManagerCompat.from(context).cancel(taskId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}