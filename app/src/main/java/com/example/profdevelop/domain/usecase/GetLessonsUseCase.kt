package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.repository.LearningRepository

class GetLessonsUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(courseId: Int): List<Lesson> = repository.getLessons(courseId)
}
