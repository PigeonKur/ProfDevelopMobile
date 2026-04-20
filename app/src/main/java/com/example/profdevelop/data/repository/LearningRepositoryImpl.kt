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
        return remoteDataSource.submitLessonAttempt(
            baseUrl = baseUrl,
            accessToken = session.accessToken,
            request = attempt.toDto()
        ).toDomain()
    }

    private suspend fun requireSession() =
        localDataSource.getStoredSession() ?: error("Нет активной сессии")
}
