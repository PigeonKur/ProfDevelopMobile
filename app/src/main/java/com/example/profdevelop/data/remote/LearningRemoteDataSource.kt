package com.example.profdevelop.data.remote

import com.example.profdevelop.data.remote.api.CoursesApi
import com.example.profdevelop.data.remote.api.ProgressApi
import com.example.profdevelop.data.remote.dto.CourseDto
import com.example.profdevelop.data.remote.dto.LessonAttemptRequestDto
import com.example.profdevelop.data.remote.dto.LessonDto
import com.example.profdevelop.data.remote.dto.LessonResultDto
import com.example.profdevelop.data.remote.dto.QuestionDto
import com.example.profdevelop.di.NetworkFactory

class LearningRemoteDataSource(
    private val networkFactory: NetworkFactory
) {
    suspend fun getAssignedCourses(baseUrl: String, accessToken: String): List<CourseDto> {
        return createCoursesApi(baseUrl, accessToken).getAssignedCourses()
    }

    suspend fun getLessons(baseUrl: String, accessToken: String, courseId: Int): List<LessonDto> {
        return createCoursesApi(baseUrl, accessToken).getLessons(courseId)
    }

    suspend fun getQuestions(baseUrl: String, accessToken: String, lessonId: Int): List<QuestionDto> {
        return createCoursesApi(baseUrl, accessToken).getQuestions(lessonId)
    }

    suspend fun submitLessonAttempt(
        baseUrl: String,
        accessToken: String,
        request: LessonAttemptRequestDto
    ): LessonResultDto {
        return createProgressApi(baseUrl, accessToken).submitLessonAttempt(request)
    }

    private fun createCoursesApi(baseUrl: String, accessToken: String): CoursesApi =
        networkFactory.createRetrofit(baseUrl, accessToken).create(CoursesApi::class.java)

    private fun createProgressApi(baseUrl: String, accessToken: String): ProgressApi =
        networkFactory.createRetrofit(baseUrl, accessToken).create(ProgressApi::class.java)
}
