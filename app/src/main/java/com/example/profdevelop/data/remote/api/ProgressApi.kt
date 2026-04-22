package com.example.profdevelop.data.remote.api

import com.example.profdevelop.data.remote.dto.LessonAttemptRequestDto
import com.example.profdevelop.data.remote.dto.LessonResultDto
import com.example.profdevelop.data.remote.dto.QuestionCheckRequestDto
import com.example.profdevelop.data.remote.dto.QuestionCheckResultDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ProgressApi {
    @POST("api/progress/question-check")
    suspend fun checkQuestion(@Body request: QuestionCheckRequestDto): QuestionCheckResultDto

    @POST("api/progress/attempt")
    suspend fun submitLessonAttempt(@Body request: LessonAttemptRequestDto): LessonResultDto
}
