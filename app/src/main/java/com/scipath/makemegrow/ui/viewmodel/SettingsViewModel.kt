package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.scipath.makemegrow.data.repository.SettingsRepository
import com.scipath.makemegrow.data.repository.SettingsRepository.Companion.DEFAULT_CONFIRMATION_OF_COMPLETION
import com.scipath.makemegrow.data.repository.SettingsRepository.Companion.DEFAULT_DEADLINE_NOTIFICATIONS
import com.scipath.makemegrow.data.repository.SettingsRepository.Companion.DEFAULT_FIRST_DAY_OF_WEEK
import com.scipath.makemegrow.data.repository.SettingsRepository.Companion.DEFAULT_TIME_FORMAT_24
import com.scipath.makemegrow.data.repository.TaskRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class SettingsViewModel(
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val confirmationOfCompletion = settingsRepository.confirmationOfCompletion.asLiveData()
    val firstDayOfWeek = settingsRepository.firstDayOfWeek.asLiveData()
    val timeFormat24 = settingsRepository.timeFormat24.asLiveData()
    val deadlineNotifications = settingsRepository.deadlineNotifications.asLiveData()

    fun isConfirmationOfCompletionEnabled(): Boolean {
        return confirmationOfCompletion.value ?: DEFAULT_CONFIRMATION_OF_COMPLETION
    }

    fun getFirstDayOfWeek(): DayOfWeek {
        return firstDayOfWeek.value ?: DEFAULT_FIRST_DAY_OF_WEEK
    }

    fun isTimeFormat24() : Boolean {
        return timeFormat24.value ?: DEFAULT_TIME_FORMAT_24
    }

    fun setConfirmationOfCompletion(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setConfirmationOfCompletion(enabled)
        }
    }

    fun setFirstDayOfWeek(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            settingsRepository.setFirstDayOfWeek(dayOfWeek)
        }
    }

    fun setTimeFormat24(timeFormat24: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTimeFormat24(timeFormat24)
        }
    }

    fun setDeadlineNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDeadlineNotifications(enabled)
            if (enabled) {
                taskRepository.scheduleAllDeadlineNotifications()
            } else {
                taskRepository.cancelAllDeadlineNotifications()
            }
        }
    }
}