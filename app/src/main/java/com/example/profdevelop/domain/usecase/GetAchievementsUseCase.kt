package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.Achievement
import com.example.profdevelop.domain.repository.LearningRepository

class GetAchievementsUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(userId: Int): List<Achievement> =
        repository.getAchievements(userId)
}
