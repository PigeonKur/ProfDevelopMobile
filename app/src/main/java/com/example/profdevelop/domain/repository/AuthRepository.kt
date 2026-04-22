package com.example.profdevelop.domain.repository

import com.example.profdevelop.domain.model.UserSession

interface AuthRepository {
    suspend fun login(email: String, password: String, rememberMe: Boolean): Result<UserSession>
    suspend fun getStoredSession(): UserSession?
    suspend fun restoreSession(): UserSession?
    suspend fun logout()
    suspend fun getApiUrl(): String
    suspend fun updateApiUrl(url: String)
}
