package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.repository.AuthRepository

class UpdateApiUrlUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(url: String) = repository.updateApiUrl(url)
}
