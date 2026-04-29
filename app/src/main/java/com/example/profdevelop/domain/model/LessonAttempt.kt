package com.example.profdevelop.domain.model

data class LessonAttempt(
    val lessonId: Int,
    val answers: List<QuestionAttempt>
)

data class QuestionAttempt(
    val questionId: Int,
    val selectedAnswerIds: List<Int> = emptyList(),
    val matchingPairs: List<MatchingAnswer> = emptyList()
)

data class MatchingAnswer(
    val leftPairId: Int,
    val rightPairId: Int
)

data class LessonResult(
    val isCompleted: Boolean,
    val score: Int,
    val maxScore: Int,
    val xpEarned: Int,
    val totalXp: Int,
    val newLevel: Int,
    val streakDays: Int,
    val previousStreak: Int,
    val streakIncreased: Boolean,
    val streakActive: Boolean,
    val newAchievements: List<Achievement>,
    val questionReviews: List<QuestionReview>
)

data class QuestionReview(
    val questionId: Int,
    val isCorrect: Boolean,
    val explanation: String?,
    val correctAnswerIds: List<Int>,
    val correctMatchingPairs: List<MatchingAnswer>
)

data class QuestionCheckResult(
    val questionId: Int,
    val isCorrect: Boolean,
    val explanation: String?,
    val correctAnswerIds: List<Int>,
    val correctMatchingPairs: List<MatchingAnswer>
)

data class Achievement(
    val id: Int,
    val title: String,
    val description: String?,
    val icon: String? = null,
    val earnedAt: String? = null,
    val conditionKey: String? = null,
    val conditionValue: Int? = null,
    val currentValue: Int? = null
) {
    val isEarned: Boolean get() = !earnedAt.isNullOrBlank()
    val hasProgress: Boolean
        get() = !isEarned && conditionValue != null && conditionValue > 0 && currentValue != null
    val progressFraction: Float
        get() = if (conditionValue != null && conditionValue > 0 && currentValue != null) {
            (currentValue.coerceAtLeast(0).toFloat() / conditionValue.toFloat()).coerceIn(0f, 1f)
        } else 0f
}



data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val fullName: String,
    val avatarUrl: String?,
    val positionTitle: String?,
    val totalXp: Int,
    val level: Int,
    val streakDays: Int,
    val tier: String? = null,
    val weeklyXp: Int = 0
)

data class XpBoostStatus(
    val isActive: Boolean,
    val remainingSeconds: Int,
    val lessonsToday: Int = 0,
    val xpToday: Int = 0,
    val isEligible: Boolean = false
)
