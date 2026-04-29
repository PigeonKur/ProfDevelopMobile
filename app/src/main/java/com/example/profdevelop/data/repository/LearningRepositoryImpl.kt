package com.example.profdevelop.data.repository

import com.example.profdevelop.data.local.AuthPreferencesDataSource
import com.example.profdevelop.data.remote.LearningRemoteDataSource
import com.example.profdevelop.data.remote.dto.toCheckDto
import com.example.profdevelop.data.remote.dto.toDomain
import com.example.profdevelop.data.remote.dto.toDto
import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.model.LessonAttempt
import com.example.profdevelop.domain.model.LessonResult
import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.domain.model.QuestionAttempt
import com.example.profdevelop.domain.model.QuestionCheckResult
import com.example.profdevelop.domain.model.Achievement
import com.example.profdevelop.domain.model.LeaderboardEntry
import com.example.profdevelop.domain.model.XpBoostStatus
import com.example.profdevelop.domain.repository.LearningRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LearningRepositoryImpl(
    private val remoteDataSource: LearningRemoteDataSource,
    private val localDataSource: AuthPreferencesDataSource
) : LearningRepository {

    override suspend fun getAssignedCourses(): List<Course> {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        return remoteDataSource.getAssignedCourses(baseUrl, session.accessToken).map { it.toDomain() }
    }

    override suspend fun getLessons(courseId: Int): List<Lesson> {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        return remoteDataSource.getLessons(baseUrl, session.accessToken, courseId).map { it.toDomain() }
    }

    override suspend fun getQuestions(lessonId: Int): List<Question> {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        return remoteDataSource.getQuestions(baseUrl, session.accessToken, lessonId).map { it.toDomain() }
    }

    override suspend fun getAchievements(userId: Int): List<Achievement> {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        return remoteDataSource.getAchievements(baseUrl, session.accessToken, userId).map {
            Achievement(
                id = it.id,
                title = it.title,
                description = it.description,
                icon = it.icon,
                earnedAt = it.earnedAt,
                conditionKey = it.conditionKey,
                conditionValue = it.conditionValue,
                currentValue = it.currentValue
            )
        }
    }

    override suspend fun checkQuestion(answer: QuestionAttempt): QuestionCheckResult {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        return remoteDataSource.checkQuestion(
            baseUrl = baseUrl,
            accessToken = session.accessToken,
            request = answer.toCheckDto()
        ).toDomain()
    }

    override suspend fun submitLessonAttempt(attempt: LessonAttempt): LessonResult {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        val result = remoteDataSource.submitLessonAttempt(
            baseUrl = baseUrl,
            accessToken = session.accessToken,
            request = attempt.toDto()
        ).toDomain()

        // Сохраняем актуальный XP/уровень/серию обратно в локальную сессию,
        // чтобы топ-бар на главной рисовался свежими данными без перелогина.
        val updatedLastActive = if (result.streakActive) {
            LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        } else {
            session.user.lastActiveDate
        }
        localDataSource.saveSession(
            session.copy(
                user = session.user.copy(
                    totalXp = result.totalXp,
                    level = result.newLevel,
                    streakDays = result.streakDays,
                    lastActiveDate = updatedLastActive
                )
            )
        )

        return result
    }

    override suspend fun getPracticeQuestions(limit: Int): List<Question> {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        return remoteDataSource.getPracticeQuestions(baseUrl, session.accessToken, limit)
            .map { it.toDomain() }
    }

    override suspend fun getLeaderboard(tier: String?): List<LeaderboardEntry> {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        return remoteDataSource.getLeaderboard(baseUrl, session.accessToken, tier).map {
            LeaderboardEntry(
                rank = (it.rank ?: 0L).toInt(),
                userId = it.userId,
                fullName = it.fullName,
                avatarUrl = it.avatarUrl,
                positionTitle = it.positionTitle,
                totalXp = it.totalXp ?: 0,
                level = it.level ?: 1,
                streakDays = it.streakDays ?: 0,
                tier = it.tier,
                weeklyXp = it.weeklyXp ?: 0
            )
        }
    }

    override suspend fun getXpBoostStatus(): XpBoostStatus {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        val dto = remoteDataSource.getXpBoostStatus(baseUrl, session.accessToken)
        return XpBoostStatus(dto.isActive, dto.remainingSeconds)
    }

    override suspend fun activateXpBoost(durationMinutes: Int): XpBoostStatus {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        val dto = remoteDataSource.activateXpBoost(baseUrl, session.accessToken, durationMinutes)
        return XpBoostStatus(dto.isActive, dto.remainingSeconds)
    }

    private suspend fun requireSession() =
        localDataSource.getStoredSession() ?: error("Нет активной сессии")
}
