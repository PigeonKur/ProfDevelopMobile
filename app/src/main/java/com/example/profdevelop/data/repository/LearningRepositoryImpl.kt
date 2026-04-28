package com.example.profdevelop.data.repository

import com.example.profdevelop.data.local.AuthPreferencesDataSource
import com.example.profdevelop.data.remote.LearningRemoteDataSource
import com.example.profdevelop.data.remote.dto.toDomain
import com.example.profdevelop.data.remote.dto.toDto
import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.model.LessonAttempt
import com.example.profdevelop.domain.model.LessonResult
import com.example.profdevelop.domain.model.Question
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

    override suspend fun submitLessonAttempt(attempt: LessonAttempt): LessonResult {
        val session = requireSession()
        val baseUrl = localDataSource.getApiUrl()
        val result = remoteDataSource.submitLessonAttempt(
            baseUrl = baseUrl,
            accessToken = session.accessToken,
            request = attempt.toDto()
        ).toDomain()

        // Сохраняем актуальный XP/уровень/серию обратно в локальную сессию,
        // чтобы экраны видели свежие данные.
        if (session.rememberMe) {
            val newLastActive = if (result.streakActive) {
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
                        lastActiveDate = newLastActive
                    )
                )
            )
        }

        return result
    }

    private suspend fun requireSession() =
        localDataSource.getStoredSession() ?: error("Нет активной сессии")
}
