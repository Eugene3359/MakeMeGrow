package com.scipath.makemegrow.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek


private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository (private val context: Context) {

    companion object {
        // Default Values
        const val DEFAULT_CONFIRMATION_OF_COMPLETION = true
        val DEFAULT_FIRST_DAY_OF_WEEK = DayOfWeek.MONDAY
        const val DEFAULT_TIME_FORMAT_24 = true

        // Keys
        private val CONFIRMATION_OF_COMPLETION_KEY = booleanPreferencesKey("confirmation_of_completion")
        private val FIRST_DAY_OF_A_WEEK_KEY = intPreferencesKey("first_day_of_week")
        private val TIME_FORMAT_24_KEY = booleanPreferencesKey("time_format_24")
    }

    val confirmationOfCompletion: Flow<Boolean> = context.dataStore.data.map { settings ->
        settings[CONFIRMATION_OF_COMPLETION_KEY] ?: DEFAULT_CONFIRMATION_OF_COMPLETION
    }

    val firstDayOfWeek: Flow<DayOfWeek> = context.dataStore.data.map { settings ->
        DayOfWeek.of(settings[FIRST_DAY_OF_A_WEEK_KEY] ?: DEFAULT_FIRST_DAY_OF_WEEK.value)
    }

    val timeFormat24: Flow<Boolean> = context.dataStore.data.map { settings ->
        settings[TIME_FORMAT_24_KEY] ?: DEFAULT_TIME_FORMAT_24
    }

    suspend fun setConfirmationOfCompletion(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[CONFIRMATION_OF_COMPLETION_KEY] = enabled
        }
    }

    suspend fun setFirstDayOfWeek(dayOfWeek: DayOfWeek) {
        context.dataStore.edit { settings ->
            settings[FIRST_DAY_OF_A_WEEK_KEY] = dayOfWeek.value
        }
    }

    suspend fun setTimeFormat24(timeFormat24: Boolean) {
        context.dataStore.edit { settings ->
            settings[TIME_FORMAT_24_KEY] = timeFormat24
        }
    }
}