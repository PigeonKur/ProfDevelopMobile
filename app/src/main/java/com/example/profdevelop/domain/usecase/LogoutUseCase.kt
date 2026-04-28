package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.repository.AuthRepository

class LogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.logout()
}
