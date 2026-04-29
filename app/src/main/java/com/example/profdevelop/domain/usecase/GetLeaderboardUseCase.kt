package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.LeaderboardEntry
import com.example.profdevelop.domain.repository.LearningRepository

class GetLeaderboardUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(tier: String? = null): List<LeaderboardEntry> =
        repository.getLeaderboard(tier)
}
