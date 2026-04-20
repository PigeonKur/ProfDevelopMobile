package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.domain.repository.LearningRepository

class GetQuestionsUseCase(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(lessonId: Int): List<Question> = repository.getQuestions(lessonId)
}
