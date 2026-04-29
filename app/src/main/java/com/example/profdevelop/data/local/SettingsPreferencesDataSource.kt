package com.example.profdevelop.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsPreferences by preferencesDataStore(name = "user_settings")

/**
 * Пользовательские настройки приложения.
 * Хранятся локально и применяются по всему приложению.
 */
data class AppSettings(
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 19,
    val dailyXpGoal: Int = 30,
    val largeText: Boolean = false,
    val lastDailyGoalCelebrateDate: String? = null
)

class SettingsPreferencesDataSource(private val context: Context) {

    private object Keys {
        val haptics = booleanPreferencesKey("haptics_enabled")
        val sound = booleanPreferencesKey("sound_enabled")
        val reminderEnabled = booleanPreferencesKey("daily_reminder_enabled")
        val reminderHour = intPreferencesKey("daily_reminder_hour")
        val dailyXpGoal = intPreferencesKey("daily_xp_goal")
        val largeText = booleanPreferencesKey("large_text")
        val lastDailyGoalCelebrateDate = stringPreferencesKey("last_daily_goal_celebrate_date")
    }

    val flow: Flow<AppSettings> = context.settingsPreferences.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map(::map)

    suspend fun setHaptics(value: Boolean) =
        context.settingsPreferences.edit { it[Keys.haptics] = value }.let { Unit }

    suspend fun setSound(value: Boolean) =
        context.settingsPreferences.edit { it[Keys.sound] = value }.let { Unit }

    suspend fun setReminderEnabled(value: Boolean) =
        context.settingsPreferences.edit { it[Keys.reminderEnabled] = value }.let { Unit }

    suspend fun setReminderHour(value: Int) =
        context.settingsPreferences.edit { it[Keys.reminderHour] = value.coerceIn(0, 23) }.let { Unit }

    suspend fun setDailyXpGoal(value: Int) =
        context.settingsPreferences.edit { it[Keys.dailyXpGoal] = value.coerceIn(10, 200) }.let { Unit }

    suspend fun setLargeText(value: Boolean) =
        context.settingsPreferences.edit { it[Keys.largeText] = value }.let { Unit }

    suspend fun setLastDailyGoalCelebrateDate(value: String?) =
        context.settingsPreferences.edit { preferences ->
            if (value.isNullOrBlank()) preferences.remove(Keys.lastDailyGoalCelebrateDate)
            else preferences[Keys.lastDailyGoalCelebrateDate] = value
        }.let { Unit }

    suspend fun resetAll() {
        context.settingsPreferences.edit { it.clear() }
    }

    private fun map(preferences: Preferences): AppSettings = AppSettings(
        hapticsEnabled = preferences[Keys.haptics] ?: true,
        soundEnabled = preferences[Keys.sound] ?: true,
        dailyReminderEnabled = preferences[Keys.reminderEnabled] ?: true,
        dailyReminderHour = preferences[Keys.reminderHour] ?: 19,
        dailyXpGoal = preferences[Keys.dailyXpGoal] ?: 30,
        largeText = preferences[Keys.largeText] ?: false,
        lastDailyGoalCelebrateDate = preferences[Keys.lastDailyGoalCelebrateDate]
    )
}
