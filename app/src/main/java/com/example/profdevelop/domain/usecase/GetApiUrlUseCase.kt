package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.repository.AuthRepository

class GetApiUrlUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): String = repository.getApiUrl()
}
