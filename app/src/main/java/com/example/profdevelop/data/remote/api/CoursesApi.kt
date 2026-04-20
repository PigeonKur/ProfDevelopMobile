package com.example.profdevelop.data.remote.api

import com.example.profdevelop.data.remote.dto.CourseDto
import com.example.profdevelop.data.remote.dto.LessonDto
import com.example.profdevelop.data.remote.dto.QuestionDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CoursesApi {
    @GET("api/progress/my-courses")
    suspend fun getAssignedCourses(): List<CourseDto>

    @GET("api/courses/{courseId}/lessons")
    suspend fun getLessons(@Path("courseId") courseId: Int): List<LessonDto>

    @GET("api/courses/lessons/{lessonId}/questions")
    suspend fun getQuestions(@Path("lessonId") lessonId: Int): List<QuestionDto>
}
