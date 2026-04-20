package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.LessonAttempt
import com.example.profdevelop.domain.model.LessonResult
import com.example.profdevelop.domain.repository.LearningRepository

class SubmitLessonAttemptUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(attempt: LessonAttempt): LessonResult =
        repository.submitLessonAttempt(attempt)
}
