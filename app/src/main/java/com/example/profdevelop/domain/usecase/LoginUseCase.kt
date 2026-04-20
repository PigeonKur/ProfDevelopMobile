package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.UserSession
import com.example.profdevelop.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        rememberMe: Boolean
    ): Result<UserSession> = repository.login(email, password, rememberMe)
}
