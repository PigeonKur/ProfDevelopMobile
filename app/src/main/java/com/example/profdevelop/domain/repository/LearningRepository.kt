package com.example.profdevelop.domain.repository

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

interface LearningRepository {
    suspend fun getAssignedCourses(): List<Course>
    suspend fun getLessons(courseId: Int): List<Lesson>
    suspend fun getQuestions(lessonId: Int): List<Question>
    suspend fun getAchievements(userId: Int): List<Achievement>
    suspend fun checkQuestion(answer: QuestionAttempt): QuestionCheckResult
    suspend fun submitLessonAttempt(attempt: LessonAttempt): LessonResult
    suspend fun getPracticeQuestions(limit: Int = 12): List<Question>
    suspend fun getLeaderboard(tier: String? = null): List<LeaderboardEntry>
    suspend fun getXpBoostStatus(): XpBoostStatus
    suspend fun activateXpBoost(durationMinutes: Int = 30): XpBoostStatus
}
