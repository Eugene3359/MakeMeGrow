package com.scipath.makemegrow.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.notification.TaskNotificationReceiver.Companion.EXTRA_TASK_ID
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(task: Task) {
        if (task.isCompleted) return


        val triggerAtMillis = getTriggerAtMillis(task)
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancel(taskId: Int) {
        val intent = Intent(context, TaskNotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun getTriggerAtMillis(task: Task): Long {
        val date = DateAndTimeConverter.secondsToDate(task.deadlineDate)
            ?: return 0
        val time = DateAndTimeConverter.secondsToTime(task.deadlineTime)
            ?: LocalTime.of(23, 59, 59, 999999999)

        return LocalDateTime
            .of(date, time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}