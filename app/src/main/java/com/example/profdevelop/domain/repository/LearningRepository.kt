package com.example.profdevelop.domain.repository

import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.model.LessonAttempt
import com.example.profdevelop.domain.model.LessonResult
import com.example.profdevelop.domain.model.Question

interface LearningRepository {
    suspend fun getAssignedCourses(): List<Course>
    suspend fun getLessons(courseId: Int): List<Lesson>
    suspend fun getQuestions(lessonId: Int): List<Question>
    suspend fun submitLessonAttempt(attempt: LessonAttempt): LessonResult
}
