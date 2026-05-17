package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.UserSession
import com.example.profdevelop.domain.repository.AuthRepository

class RestoreSessionUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): UserSession? = runCatching {
        repository.restoreSession()
    }.getOrNull()
}
