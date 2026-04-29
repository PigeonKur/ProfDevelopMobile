package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.XpBoostStatus
import com.example.profdevelop.domain.repository.LearningRepository

class GetXpBoostStatusUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(dailyXpGoal: Int? = null): XpBoostStatus = repository.getXpBoostStatus(dailyXpGoal)
}

class ActivateXpBoostUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(durationMinutes: Int = 30, dailyXpGoal: Int? = null): XpBoostStatus =
        repository.activateXpBoost(durationMinutes, dailyXpGoal)
}