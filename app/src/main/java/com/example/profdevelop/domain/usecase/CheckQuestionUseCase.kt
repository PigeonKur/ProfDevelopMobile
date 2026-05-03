package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.QuestionAttempt
import com.example.profdevelop.domain.model.QuestionCheckResult
import com.example.profdevelop.domain.repository.LearningRepository

class CheckQuestionUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(answer: QuestionAttempt, mode: String? = null): QuestionCheckResult =
        repository.checkQuestion(answer, mode)
}
