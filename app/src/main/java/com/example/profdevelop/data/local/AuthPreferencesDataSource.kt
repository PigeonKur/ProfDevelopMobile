package com.example.profdevelop.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.profdevelop.BuildConfig
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.authPreferences by preferencesDataStore(name = "auth_preferences")

class AuthPreferencesDataSource(private val context: Context) {

    private object Keys {
        val apiUrl = stringPreferencesKey("api_url")
        val rememberMe = booleanPreferencesKey("remember_me")
        val accessToken = stringPreferencesKey("access_token")
        val refreshToken = stringPreferencesKey("refresh_token")
        val userId = intPreferencesKey("user_id")
        val fullName = stringPreferencesKey("full_name")
        val email = stringPreferencesKey("email")
        val role = stringPreferencesKey("role")
        val positionTitle = stringPreferencesKey("position_title")
        val departmentName = stringPreferencesKey("department_name")
        val totalXp = intPreferencesKey("total_xp")
        val level = intPreferencesKey("level")
        val streakDays = intPreferencesKey("streak_days")
        val avatarUrl = stringPreferencesKey("avatar_url")
    }

    val sessionFlow: Flow<UserSession?> = context.authPreferences.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map(::mapSession)

    suspend fun getStoredSession(): UserSession? = sessionFlow.first()

    suspend fun getApiUrl(): String = context.authPreferences.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[Keys.apiUrl] ?: BuildConfig.API_BASE_URL }
        .first()

    suspend fun updateApiUrl(url: String) {
        context.authPreferences.edit { preferences ->
            preferences[Keys.apiUrl] = normalizeUrl(url)
        }
    }

    suspend fun saveSession(session: UserSession) {
        context.authPreferences.edit { prefs ->
            prefs[Keys.rememberMe] = session.rememberMe
            prefs[Keys.accessToken] = session.accessToken
            prefs[Keys.refreshToken] = session.refreshToken
            prefs[Keys.userId] = session.user.id
            prefs[Keys.fullName] = session.user.fullName
            prefs[Keys.email] = session.user.email
            prefs[Keys.role] = session.user.role
            session.user.positionTitle?.let { prefs[Keys.positionTitle] = it }
            session.user.departmentName?.let { prefs[Keys.departmentName] = it }
            prefs[Keys.totalXp] = session.user.totalXp
            prefs[Keys.level] = session.user.level
            prefs[Keys.streakDays] = session.user.streakDays
            session.user.avatarUrl?.let { prefs[Keys.avatarUrl] = it }
        }
    }

    suspend fun clearSession() {
        context.authPreferences.edit { prefs ->
            prefs.remove(Keys.rememberMe)
            prefs.remove(Keys.accessToken)
            prefs.remove(Keys.refreshToken)
            prefs.remove(Keys.userId)
            prefs.remove(Keys.fullName)
            prefs.remove(Keys.email)
            prefs.remove(Keys.role)
            prefs.remove(Keys.positionTitle)
            prefs.remove(Keys.departmentName)
            prefs.remove(Keys.totalXp)
            prefs.remove(Keys.level)
            prefs.remove(Keys.streakDays)
            prefs.remove(Keys.avatarUrl)
        }
    }

    private fun mapSession(preferences: Preferences): UserSession? {
        val accessToken = preferences[Keys.accessToken] ?: return null
        val refreshToken = preferences[Keys.refreshToken] ?: return null
        val fullName = preferences[Keys.fullName] ?: return null
        val email = preferences[Keys.email] ?: return null
        val role = preferences[Keys.role] ?: return null
        val userId = preferences[Keys.userId] ?: return null

        return UserSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            rememberMe = preferences[Keys.rememberMe] ?: false,
            user = UserProfile(
                id = userId,
                fullName = fullName,
                email = email,
                role = role,
                positionTitle = preferences[Keys.positionTitle],
                departmentName = preferences[Keys.departmentName],
                totalXp = preferences[Keys.totalXp] ?: 0,
                level = preferences[Keys.level] ?: 1,
                streakDays = preferences[Keys.streakDays] ?: 0,
                avatarUrl = preferences[Keys.avatarUrl]
            )
        )
    }

    private fun normalizeUrl(value: String): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return BuildConfig.API_BASE_URL
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }
}
