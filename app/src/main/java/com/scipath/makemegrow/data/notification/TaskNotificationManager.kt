package com.scipath.makemegrow.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.scipath.makemegrow.R
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.ui.activity.MainActivity
import com.scipath.makemegrow.ui.activity.TaskActivity

class TaskNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "task_notifications"
    }

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.deadline),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.deadline_notifications)
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showTaskNotification(task: Task) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_TASK_ID, task.id)
        }

        val openPendingIntent = PendingIntent.getActivity(
            context,
            task.id,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val editIntent = Intent(context, TaskActivity::class.java).apply {
            putExtra("task", task)
            putExtra("cancel_notification", true)
        }

        val editPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(editIntent)
            getPendingIntent(
                task.id + 100_000,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val completeIntent = Intent(context, TaskNotificationActionReceiver::class.java).apply {
            action = TaskNotificationActionReceiver.ACTION_COMPLETE
            putExtra(TaskNotificationActionReceiver.EXTRA_TASK_ID, task.id)
        }

        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            task.id + 100_000_000,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setColor(ContextCompat.getColor(context, R.color.black))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.name)
            .setContentText(
                DateAndTimeConverter.timeToString(
                    DateAndTimeConverter.secondsToTime(task.deadlineTime),
                    true,
                    context
                )
            )
            .setContentIntent(openPendingIntent)
            .addAction(
                0,
                context.getString(R.string.edit),
                editPendingIntent
            )
            .addAction(
                0,
                context.getString(R.string.complete),
                completePendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(task.id, notification)
    }
}