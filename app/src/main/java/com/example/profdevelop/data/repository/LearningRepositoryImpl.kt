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
            Achievement(it.id, it.title, it.description, it.icon, it.earnedAt)
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

    private suspend fun requireSession() =
        localDataSource.getStoredSession() ?: error("Нет активной сессии")
}
