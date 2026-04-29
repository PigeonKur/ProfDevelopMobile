package com.example.profdevelop.data.repository

import com.example.profdevelop.data.local.AuthPreferencesDataSource
import com.example.profdevelop.data.remote.AuthRemoteDataSource
import com.example.profdevelop.data.remote.dto.LoginRequestDto
import com.example.profdevelop.data.remote.dto.toDomain
import com.example.profdevelop.domain.model.UserSession
import com.example.profdevelop.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthPreferencesDataSource
) : AuthRepository {

    override suspend fun getStoredSession(): UserSession? = localDataSource.getStoredSession()

    override suspend fun login(
        email: String,
        password: String,
        rememberMe: Boolean
    ): Result<UserSession> = runCatching {
        val baseUrl = localDataSource.getApiUrl()
        val session = remoteDataSource.login(
            baseUrl = baseUrl,
            request = LoginRequestDto(
                email = email.trim(),
                password = password,
                deviceInfo = "Android/${android.os.Build.MODEL}"
            )
        ).toDomain(rememberMe)

        if (rememberMe) {
            localDataSource.saveSession(session)
        } else {
            localDataSource.clearSession()
        }

        session
    }

    override suspend fun restoreSession(): UserSession? {
        val stored = localDataSource.sessionFlow.firstOrNull() ?: return null
        if (!stored.rememberMe) return null

        return runCatching {
            val refreshed = remoteDataSource.refresh(
                baseUrl = localDataSource.getApiUrl(),
                refreshToken = stored.refreshToken
            ).toDomain(rememberMe = true)

            // Если бэкенд не вернул lastActiveDate (старая версия API), сохраняем
            // ранее записанное значение, чтобы серия не сбрасывалась после перезахода.
            val merged = if (refreshed.user.lastActiveDate.isNullOrBlank()) {
                refreshed.copy(
                    user = refreshed.user.copy(lastActiveDate = stored.user.lastActiveDate)
                )
            } else {
                refreshed
            }

            localDataSource.saveSession(merged)
            merged
        }.getOrElse { error ->
            // 401/403 — токен реально протух, чистим. Любая сетевая ошибка
            // (нет интернета, API не запущен, таймаут) — возвращаем
            // существующую сессию, чтобы юзер продолжил в офлайне без вылогина.
            val isAuthError = error is HttpException &&
                (error.code() == 401 || error.code() == 403)
            if (isAuthError) {
                localDataSource.clearSession()
                null
            } else {
                stored
            }
        }
    }

    override suspend fun logout() {
        val session = localDataSource.sessionFlow.firstOrNull()
        runCatching {
            session?.refreshToken?.let { refreshToken ->
                remoteDataSource.logout(
                    baseUrl = localDataSource.getApiUrl(),
                    refreshToken = refreshToken
                )
            }
        }
        localDataSource.clearSession()
    }

    override suspend fun getApiUrl(): String = localDataSource.getApiUrl()

    override suspend fun updateApiUrl(url: String) {
        localDataSource.updateApiUrl(url)
    }
}
