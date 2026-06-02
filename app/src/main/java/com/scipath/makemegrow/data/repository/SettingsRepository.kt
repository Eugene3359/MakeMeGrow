package com.scipath.makemegrow.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository (private val context: Context) {

    companion object {
        // Default Values
        const val DEFAULT_CONFIRMATION_OF_COMPLETION = true

        // Keys
        private val CONFIRMATION_OF_COMPLETION_KEY = booleanPreferencesKey("completion_confirmation")
    }

    suspend fun setConfirmationOfCompletion(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[CONFIRMATION_OF_COMPLETION_KEY] = enabled
        }
    }

    val confirmationOfCompletion: Flow<Boolean> = context.dataStore.data.map {
        settings -> settings[CONFIRMATION_OF_COMPLETION_KEY] ?: DEFAULT_CONFIRMATION_OF_COMPLETION
    }
}