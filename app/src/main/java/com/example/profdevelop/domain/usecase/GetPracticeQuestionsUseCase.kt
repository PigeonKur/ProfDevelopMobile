package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.domain.repository.LearningRepository

class GetPracticeQuestionsUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(limit: Int = 12): List<Question> =
        repository.getPracticeQuestions(limit)
}
