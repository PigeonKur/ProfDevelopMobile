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
    val icon: String?,
    val earnedAt: String?
)
