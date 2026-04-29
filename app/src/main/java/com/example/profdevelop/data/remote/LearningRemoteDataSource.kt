package com.example.profdevelop.data.remote

import com.example.profdevelop.data.remote.api.CoursesApi
import com.example.profdevelop.data.remote.api.ProgressApi
import com.example.profdevelop.data.remote.api.UsersApi
import com.example.profdevelop.data.remote.dto.AchievementDto
import com.example.profdevelop.data.remote.dto.CourseDto
import com.example.profdevelop.data.remote.dto.LeaderboardEntryDto
import com.example.profdevelop.data.remote.dto.LessonAttemptRequestDto
import com.example.profdevelop.data.remote.dto.LessonDto
import com.example.profdevelop.data.remote.dto.LessonResultDto
import com.example.profdevelop.data.remote.dto.QuestionDto
import com.example.profdevelop.data.remote.dto.QuestionCheckRequestDto
import com.example.profdevelop.data.remote.dto.QuestionCheckResultDto
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

    suspend fun getAchievements(baseUrl: String, accessToken: String, userId: Int): List<AchievementDto> {
        return createUsersApi(baseUrl, accessToken).getAchievements(userId)
    }

    suspend fun getLeaderboard(
        baseUrl: String,
        accessToken: String,
        tier: String? = null
    ): List<LeaderboardEntryDto> {
        return createUsersApi(baseUrl, accessToken).getLeaderboard(tier)
    }

        suspend fun getXpBoostStatus(
        baseUrl: String,
        accessToken: String,
        dailyXpGoal: Int? = null
    ): com.example.profdevelop.data.remote.dto.XpBoostStatusDto {
        return createProgressApi(baseUrl, accessToken).getXpBoostStatus(dailyXpGoal)
    }

        suspend fun activateXpBoost(
        baseUrl: String,
        accessToken: String,
        durationMinutes: Int = 30,
        dailyXpGoal: Int? = null
    ): com.example.profdevelop.data.remote.dto.XpBoostStatusDto {
        return createProgressApi(baseUrl, accessToken)
            .activateXpBoost(com.example.profdevelop.data.remote.dto.ActivateBoostRequestDto(durationMinutes, dailyXpGoal))
    }

    suspend fun checkQuestion(
        baseUrl: String,
        accessToken: String,
        request: QuestionCheckRequestDto
    ): QuestionCheckResultDto {
        return createProgressApi(baseUrl, accessToken).checkQuestion(request)
    }

    suspend fun submitLessonAttempt(
        baseUrl: String,
        accessToken: String,
        request: LessonAttemptRequestDto
    ): LessonResultDto {
        return createProgressApi(baseUrl, accessToken).submitLessonAttempt(request)
    }

    suspend fun getPracticeQuestions(
        baseUrl: String,
        accessToken: String,
        limit: Int
    ): List<QuestionDto> {
        return createProgressApi(baseUrl, accessToken).getPracticeQuestions(limit)
    }

    private fun createCoursesApi(baseUrl: String, accessToken: String): CoursesApi =
        networkFactory.createRetrofit(baseUrl, accessToken).create(CoursesApi::class.java)

    private fun createProgressApi(baseUrl: String, accessToken: String): ProgressApi =
        networkFactory.createRetrofit(baseUrl, accessToken).create(ProgressApi::class.java)

    private fun createUsersApi(baseUrl: String, accessToken: String): UsersApi =
        networkFactory.createRetrofit(baseUrl, accessToken).create(UsersApi::class.java)
}
