package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.UserSession
import com.example.profdevelop.domain.repository.AuthRepository

class GetStoredSessionUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): UserSession? = repository.getStoredSession()
}
