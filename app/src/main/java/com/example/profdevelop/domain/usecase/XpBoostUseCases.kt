package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.XpBoostStatus
import com.example.profdevelop.domain.repository.LearningRepository

class GetXpBoostStatusUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(): XpBoostStatus = repository.getXpBoostStatus()
}

class ActivateXpBoostUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(durationMinutes: Int = 30): XpBoostStatus =
        repository.activateXpBoost(durationMinutes)
}
