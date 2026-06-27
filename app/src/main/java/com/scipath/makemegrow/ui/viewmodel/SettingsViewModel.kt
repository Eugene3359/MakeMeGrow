package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.scipath.makemegrow.data.repository.SettingsRepository
import com.scipath.makemegrow.data.repository.SettingsRepository.Companion.DEFAULT_CONFIRMATION_OF_COMPLETION
import com.scipath.makemegrow.data.repository.SettingsRepository.Companion.DEFAULT_FIRST_DAY_OF_WEEK
import com.scipath.makemegrow.data.repository.SettingsRepository.Companion.DEFAULT_TIME_FORMAT_24
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val confirmationOfCompletion = repository.confirmationOfCompletion.asLiveData()
    val firstDayOfWeek = repository.firstDayOfWeek.asLiveData()
    val timeFormat24 = repository.timeFormat24.asLiveData()

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
            repository.setConfirmationOfCompletion(enabled)
        }
    }

    fun setFirstDayOfWeek(dayOfWeek: DayOfWeek) {
        viewModelScope.launch {
            repository.setFirstDayOfWeek(dayOfWeek)
        }
    }

    fun setTimeFormat24(timeFormat24: Boolean) {
        viewModelScope.launch {
            repository.setTimeFormat24(timeFormat24)
        }
    }
}