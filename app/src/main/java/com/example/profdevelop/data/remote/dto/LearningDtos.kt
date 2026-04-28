package com.example.profdevelop.data.remote.dto

import com.example.profdevelop.domain.model.Achievement
import com.example.profdevelop.domain.model.AnswerOption
import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.model.LessonAttempt
import com.example.profdevelop.domain.model.LessonResult
import com.example.profdevelop.domain.model.MatchingAnswer
import com.example.profdevelop.domain.model.MatchingPair
import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.domain.model.QuestionAttempt
import com.example.profdevelop.domain.model.QuestionCheckResult
import com.example.profdevelop.domain.model.QuestionReview
import com.google.gson.annotations.SerializedName

data class CourseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int,
    @SerializedName("progressPct") val progressPct: Int?,
    @SerializedName("totalLessons") val totalLessons: Int,
    @SerializedName("completedLessons") val completedLessons: Int?,
    @SerializedName("isMandatory") val isMandatory: Boolean?,
    @SerializedName("deadline") val deadline: String?
)

data class LessonDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("orderIndex") val orderIndex: Int,
    @SerializedName("xpReward") val xpReward: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("lessonType") val lessonType: String,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int,
    @SerializedName("isCompleted") val isCompleted: Boolean,
    @SerializedName("isUnlocked") val isUnlocked: Boolean,
    @SerializedName("score") val score: Int?,
    @SerializedName("maxScore") val maxScore: Int?
)

data class QuestionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String,
    @SerializedName("xpValue") val xpValue: Int,
    @SerializedName("hint") val hint: String?,
    @SerializedName("explanationCorrect") val explanationCorrect: String?,
    @SerializedName("explanationWrong") val explanationWrong: String?,
    @SerializedName("answers") val answers: List<AnswerDto>,
    @SerializedName("matchingPairs") val matchingPairs: List<MatchingPairDto>
)

data class AnswerDto(
    @SerializedName("id") val id: Int,
    @SerializedName("text") val text: String,
    @SerializedName("orderIndex") val orderIndex: Int
)

data class MatchingPairDto(
    @SerializedName("id") val id: Int,
    @SerializedName("leftText") val leftText: String,
    @SerializedName("rightText") val rightText: String,
    @SerializedName("orderIndex") val orderIndex: Int
)

data class LessonAttemptRequestDto(
    @SerializedName("lessonId") val lessonId: Int,
    @SerializedName("answers") val answers: List<QuestionAttemptDto>
)

data class QuestionAttemptDto(
    @SerializedName("questionId") val questionId: Int,
    @SerializedName("selectedAnswerIds") val selectedAnswerIds: List<Int>?,
    @SerializedName("matchingPairs") val matchingPairs: List<MatchingAnswerDto>?
)

data class QuestionCheckRequestDto(
    @SerializedName("questionId") val questionId: Int,
    @SerializedName("selectedAnswerIds") val selectedAnswerIds: List<Int>?,
    @SerializedName("matchingPairs") val matchingPairs: List<MatchingAnswerDto>?
)

data class MatchingAnswerDto(
    @SerializedName("leftPairId") val leftPairId: Int,
    @SerializedName("rightPairId") val rightPairId: Int
)

data class LessonResultDto(
    @SerializedName("isCompleted") val isCompleted: Boolean,
    @SerializedName("score") val score: Int,
    @SerializedName("maxScore") val maxScore: Int,
    @SerializedName("xpEarned") val xpEarned: Int,
    @SerializedName("totalXp") val totalXp: Int,
    @SerializedName("newLevel") val newLevel: Int,
    @SerializedName("streakDays") val streakDays: Int,
    @SerializedName("previousStreak") val previousStreak: Int? = null,
    @SerializedName("streakIncreased") val streakIncreased: Boolean? = null,
    @SerializedName("streakActive") val streakActive: Boolean? = null,
    @SerializedName("newAchievements") val newAchievements: List<AchievementDto>,
    @SerializedName("questions") val questions: List<QuestionReviewDto>
)

data class AchievementDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("earnedAt") val earnedAt: String?
)

data class QuestionReviewDto(
    @SerializedName("questionId") val questionId: Int,
    @SerializedName("isCorrect") val isCorrect: Boolean,
    @SerializedName("explanation") val explanation: String?,
    @SerializedName("correctAnswerIds") val correctAnswerIds: List<Int>,
    @SerializedName("correctMatchingPairs") val correctMatchingPairs: List<MatchingAnswerDto>
)

data class QuestionCheckResultDto(
    @SerializedName("questionId") val questionId: Int,
    @SerializedName("isCorrect") val isCorrect: Boolean,
    @SerializedName("explanation") val explanation: String?,
    @SerializedName("correctAnswerIds") val correctAnswerIds: List<Int>,
    @SerializedName("correctMatchingPairs") val correctMatchingPairs: List<MatchingAnswerDto>
)

fun CourseDto.toDomain(): Course = Course(
    id = id,
    title = title,
    description = description,
    category = category,
    difficulty = difficulty,
    estimatedMinutes = estimatedMinutes,
    progressPercent = progressPct ?: 0,
    totalLessons = totalLessons,
    completedLessons = completedLessons ?: 0,
    isMandatory = isMandatory ?: false,
    deadline = deadline
)

fun LessonDto.toDomain(): Lesson = Lesson(
    id = id,
    title = title,
    orderIndex = orderIndex,
    xpReward = xpReward,
    description = description,
    lessonType = lessonType,
    estimatedMinutes = estimatedMinutes,
    isCompleted = isCompleted,
    isUnlocked = isUnlocked,
    score = score,
    maxScore = maxScore
)

fun QuestionDto.toDomain(): Question = Question(
    id = id,
    type = type,
    text = text,
    xpValue = xpValue,
    hint = hint,
    explanationCorrect = explanationCorrect,
    explanationWrong = explanationWrong,
    answers = answers.map { AnswerOption(it.id, it.text, it.orderIndex) },
    matchingPairs = matchingPairs.map { MatchingPair(it.id, it.leftText, it.rightText, it.orderIndex) }
)

fun LessonAttempt.toDto(): LessonAttemptRequestDto = LessonAttemptRequestDto(
    lessonId = lessonId,
    answers = answers.map { answer ->
        QuestionAttemptDto(
            questionId = answer.questionId,
            selectedAnswerIds = answer.selectedAnswerIds.ifEmpty { null },
            matchingPairs = answer.matchingPairs.takeIf { it.isNotEmpty() }?.map {
                MatchingAnswerDto(it.leftPairId, it.rightPairId)
            }
        )
    }
)

fun QuestionAttempt.toCheckDto(): QuestionCheckRequestDto = QuestionCheckRequestDto(
    questionId = questionId,
    selectedAnswerIds = selectedAnswerIds.ifEmpty { null },
    matchingPairs = matchingPairs.takeIf { it.isNotEmpty() }?.map {
        MatchingAnswerDto(it.leftPairId, it.rightPairId)
    }
)

fun LessonResultDto.toDomain(): LessonResult = LessonResult(
    isCompleted = isCompleted,
    score = score,
    maxScore = maxScore,
    xpEarned = xpEarned,
    totalXp = totalXp,
    newLevel = newLevel,
    streakDays = streakDays,
    previousStreak = previousStreak ?: streakDays,
    streakIncreased = streakIncreased ?: false,
    streakActive = streakActive ?: (streakDays > 0),
    newAchievements = newAchievements.map {
        Achievement(it.id, it.title, it.description, it.icon, it.earnedAt)
    },
    questionReviews = questions.map {
        QuestionReview(
            questionId = it.questionId,
            isCorrect = it.isCorrect,
            explanation = it.explanation,
            correctAnswerIds = it.correctAnswerIds,
            correctMatchingPairs = it.correctMatchingPairs.map { pair ->
                MatchingAnswer(pair.leftPairId, pair.rightPairId)
            }
        )
    }
)

fun QuestionCheckResultDto.toDomain(): QuestionCheckResult = QuestionCheckResult(
    questionId = questionId,
    isCorrect = isCorrect,
    explanation = explanation,
    correctAnswerIds = correctAnswerIds,
    correctMatchingPairs = correctMatchingPairs.map {
        MatchingAnswer(it.leftPairId, it.rightPairId)
    }
)
