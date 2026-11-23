package com.scipath.makemegrow.data.repository

import com.scipath.makemegrow.data.converter.DateAndTimeConverter
import com.scipath.makemegrow.data.dao.TaskDao
import com.scipath.makemegrow.data.model.Task
import com.scipath.makemegrow.data.model.Task.RepeatType.NO_REPEAT
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_DAY
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_MONTH
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_WEEK
import com.scipath.makemegrow.data.model.Task.RepeatType.ONCE_A_YEAR
import com.scipath.makemegrow.data.model.Task.RepeatType.ON_WEEKDAYS
import com.scipath.makemegrow.data.model.Task.RepeatType.ON_WEEKENDS
import com.scipath.makemegrow.data.notification.NotificationScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepository(
    private val taskDao: TaskDao,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) {

    private val currentDateTime = MutableStateFlow(
        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
    )

    val allTasks: Flow<List<Task>> = taskDao.getAll()
    val overdueTasks: Flow<List<Task>> = currentDateTime.flatMapLatest { now ->
        taskDao.getBeforeDeadline(
            DateAndTimeConverter.dateToSeconds(now.toLocalDate()),
            DateAndTimeConverter.timeToSeconds(now.toLocalTime())
        )
    }
    val todayTasks: Flow<List<Task>> = currentDateTime.flatMapLatest { now ->
        taskDao.getBetweenDeadlines(
            DateAndTimeConverter.dateToSeconds(now.toLocalDate()),
            DateAndTimeConverter.timeToSeconds(now.toLocalTime()),
            DateAndTimeConverter.dateToSeconds(now.toLocalDate()),
            DateAndTimeConverter.NO_TIME
        )
    }
    val tomorrowTasks: Flow<List<Task>> = currentDateTime.flatMapLatest { now ->
        val today = now.toLocalDate()
        taskDao.getByDeadlineDate(
            DateAndTimeConverter.dateToSeconds(tomorrowDate(today))
        )
    }
    val thisWeekTasks: Flow<List<Task>> = combine(
        currentDateTime,
        settingsRepository.firstDayOfWeek
    ) { now, firstDayOfWeek ->
        val today = now.toLocalDate()
        Pair(
            tomorrowDate(today),
            endOfThisWeek(today, firstDayOfWeek)
        )
    }.flatMapLatest { (tomorrowDate, endOfThisWeek) ->
        taskDao.getBetweenDeadlines(
            DateAndTimeConverter.dateToSeconds(tomorrowDate),
            DateAndTimeConverter.dateToSeconds(endOfThisWeek)
        )
    }
    val nextWeekTasks: Flow<List<Task>> = combine(
        currentDateTime,
        settingsRepository.firstDayOfWeek
    ) { now, firstDayOfWeek ->
        val today = now.toLocalDate()
        Triple(
            today,
            endOfThisWeek(today, firstDayOfWeek),
            endOfNextWeek(today, firstDayOfWeek)
        )
    }.flatMapLatest { (today, endOfThisWeek, endOfNextWeek) ->
        taskDao.getBetweenDeadlines(
            DateAndTimeConverter.dateToSeconds(
                maxOf(tomorrowDate(today), endOfThisWeek)
            ),
            DateAndTimeConverter.dateToSeconds(endOfNextWeek)
        )
    }
    val thisMonthTasks: Flow<List<Task>> = combine(
        currentDateTime,
        settingsRepository.firstDayOfWeek
    ) { now, firstDayOfWeek ->
        val today = now.toLocalDate()
        Pair(
            endOfNextWeek(today, firstDayOfWeek),
            endOfThisMonth(today)
        )
    }.flatMapLatest { (endOfNextWeek, endOfThisMonth) ->
        taskDao.getBetweenDeadlines(
            DateAndTimeConverter.dateToSeconds(endOfNextWeek),
            DateAndTimeConverter.dateToSeconds(endOfThisMonth)
        )
    }
    val nextMonthTasks: Flow<List<Task>> = combine(
        currentDateTime,
        settingsRepository.firstDayOfWeek
    ) { now, firstDayOfWeek ->
        val today = now.toLocalDate()
        Triple(
            endOfNextWeek(today, firstDayOfWeek),
            endOfThisMonth(today),
            endOfNextMonth(today)
        )
    }.flatMapLatest { (endOfNextWeek, endOfThisMonth, endOfNextMonth) ->
        taskDao.getBetweenDeadlines(
            DateAndTimeConverter.dateToSeconds(
                maxOf(endOfNextWeek, endOfThisMonth)
            ),
            DateAndTimeConverter.dateToSeconds(endOfNextMonth)
        )
    }
    val laterTasks: Flow<List<Task>> = currentDateTime.flatMapLatest { now ->
        val today = now.toLocalDate()
        taskDao.getAfterDeadline(
            DateAndTimeConverter.dateToSeconds(endOfNextMonth(today)),
            DateAndTimeConverter.NO_TIME
        )
    }

    // CRUD
    fun getById(id: Int): Task? {
        return taskDao.getById(id)
    }

    suspend fun addTask(task: Task) {
        val id = taskDao.insert(task)
        val savedTask = task.copy(id = id.toInt())
        scheduleDeadlineNotification(savedTask)
    }

    suspend fun updateTask(task: Task) {
        cancelDeadlineNotification(task)
        taskDao.updateTask(task)
        if (!task.isCompleted) {
            scheduleDeadlineNotification(task)
        }
    }

    suspend fun upsertTask(task: Task) {
        cancelDeadlineNotification(task)
        val id = taskDao.upsertTask(task)
        val savedTask = task.copy(id = id.toInt())
        if (!savedTask.isCompleted) {
            scheduleDeadlineNotification(savedTask)
        }
    }

    suspend fun deleteTask(task: Task) {
        cancelDeadlineNotification(task)
        taskDao.delete(task)
    }

    suspend fun deleteTasks(tasks: List<Task>) {
        tasks.forEach {
            cancelDeadlineNotification(it)
        }
        taskDao.delete(tasks)
    }

    suspend fun clear() {
        cancelAllDeadlineNotifications()
        taskDao.clear()
    }

    // Refresh
    fun refreshTasks() {
        currentDateTime.value = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
    }

    // Complete
    suspend fun completeTask(task: Task, isCompleted: Boolean) {
        if (task.repeatType == NO_REPEAT) {
            task.isCompleted = isCompleted
        } else {
            if (isCompleted) {
                task.isCompleted = false
                task.deadlineDate = getNextDeadline(task)
            }
        }
        updateTask(task)
    }

    private fun getNextDeadline(task: Task): Long {
        val deadlineDate: LocalDate = DateAndTimeConverter.secondsToDate(task.deadlineDate)
            ?: return task.deadlineDate

        val nextDate = when (task.repeatType) {
            NO_REPEAT -> deadlineDate
            ONCE_A_DAY -> deadlineDate.plusDays(1)
            ON_WEEKDAYS -> deadlineDate.plusDays(
                if (deadlineDate.dayOfWeek.value < 5) 1
                else 8L - deadlineDate.dayOfWeek.value
            )
            ON_WEEKENDS -> deadlineDate.plusDays(
                if (deadlineDate.dayOfWeek.value == 6) 1
                else if (deadlineDate.dayOfWeek.value == 7) 6
                else 6L - deadlineDate.dayOfWeek.value
            )
            ONCE_A_WEEK -> deadlineDate.plusWeeks(1)
            ONCE_A_MONTH -> deadlineDate.plusMonths(1)
            ONCE_A_YEAR -> deadlineDate.plusYears(1)
        }

        return DateAndTimeConverter.dateToSeconds(nextDate)
    }

    // Filter
    fun filterByCompletion(tasks: Flow<List<Task>>, isCompleted: Boolean): Flow<List<Task>> {
        return tasks.map { list ->
            list.filter { it.isCompleted == isCompleted }
        }
    }

    // Notifications
    private suspend fun scheduleDeadlineNotification(task: Task) {
        if (settingsRepository.deadlineNotifications.first()) {
            notificationScheduler.schedule(task)
        }
    }

    private fun cancelDeadlineNotification(task: Task) {
        notificationScheduler.cancel(task.id)
    }

    suspend fun scheduleAllDeadlineNotifications() {
        allTasks.first().forEach { task ->
            scheduleDeadlineNotification(task)
        }
    }

    suspend fun cancelAllDeadlineNotifications() {
        allTasks.first().forEach { task ->
            cancelDeadlineNotification(task)
        }
    }

    // Dates
    private fun tomorrowDate(today: LocalDate): LocalDate {
        return today.plusDays(1)
    }

    private fun endOfThisWeek(today: LocalDate, firstDayOfWeek: DayOfWeek): LocalDate {
        val lastDayOfWeek = firstDayOfWeek.minus(1)
        val daysUntilLastDayOfWeek = (lastDayOfWeek.value - today.dayOfWeek.value + 7) % 7
        return today.plusDays(daysUntilLastDayOfWeek.toLong())
    }

    private fun endOfNextWeek(today: LocalDate, firstDayOfWeek: DayOfWeek): LocalDate {
        return endOfThisWeek(today, firstDayOfWeek).plusWeeks(1)
    }

    private fun endOfThisMonth(today: LocalDate): LocalDate {
        return today.withDayOfMonth(today.month.length(today.isLeapYear))
    }

    private fun endOfNextMonth(today: LocalDate): LocalDate {
        val nextMonth: LocalDate = today.plusMonths(1)
        return nextMonth.withDayOfMonth(nextMonth.month.length(nextMonth.isLeapYear))
    }
}