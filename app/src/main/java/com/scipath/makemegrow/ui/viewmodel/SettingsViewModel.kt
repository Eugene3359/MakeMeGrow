package com.scipath.makemegrow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.scipath.makemegrow.data.repository.SettingsRepository
import com.scipath.makemegrow.data.repository.SettingsRepository.Companion.DEFAULT_CONFIRMATION_OF_COMPLETION
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val confirmationOfCompletion = repository.confirmationOfCompletion.asLiveData()

    fun isConfirmationOfCompletionEnabled(): Boolean {
        return confirmationOfCompletion.value ?: DEFAULT_CONFIRMATION_OF_COMPLETION
    }

    fun setConfirmationOfCompletion(enabled: Boolean) {
        viewModelScope.launch {
            repository.setConfirmationOfCompletion(enabled)
        }
    }
}