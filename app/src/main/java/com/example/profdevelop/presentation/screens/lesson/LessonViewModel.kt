package com.example.profdevelop.presentation.screens.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.model.LessonAttempt
import com.example.profdevelop.domain.model.LessonResult
import com.example.profdevelop.domain.model.MatchingAnswer
import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.domain.model.QuestionAttempt
import com.example.profdevelop.domain.usecase.GetQuestionsUseCase
import com.example.profdevelop.domain.usecase.SubmitLessonAttemptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LessonViewModel(
    private val lessonId: Int,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val submitLessonAttemptUseCase: SubmitLessonAttemptUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LessonUiState())
    val state: StateFlow<LessonUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun selectAnswer(questionId: Int, answerId: Int) {
        val updated = _state.value.selectedAnswers.toMutableMap()
        updated[questionId] = setOf(answerId)
        _state.value = _state.value.copy(selectedAnswers = updated)
    }

    fun selectMatching(questionId: Int, leftPairId: Int, rightPairId: Int) {
        val updated = _state.value.selectedMatches.toMutableMap()
        val questionMap = updated[questionId]?.toMutableMap() ?: mutableMapOf()
        questionMap[leftPairId] = rightPairId
        updated[questionId] = questionMap
        _state.value = _state.value.copy(selectedMatches = updated)
    }

    fun nextQuestion() {
        val next = (_state.value.currentQuestionIndex + 1).coerceAtMost(_state.value.questions.lastIndex)
        _state.value = _state.value.copy(currentQuestionIndex = next)
    }

    fun previousQuestion() {
        val prev = (_state.value.currentQuestionIndex - 1).coerceAtLeast(0)
        _state.value = _state.value.copy(currentQuestionIndex = prev)
    }

    fun submit() {
        val questions = _state.value.questions
        if (questions.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            runCatching {
                val attempt = LessonAttempt(
                    lessonId = lessonId,
                    answers = questions.map { question ->
                        QuestionAttempt(
                            questionId = question.id,
                            selectedAnswerIds = _state.value.selectedAnswers[question.id]?.toList().orEmpty(),
                            matchingPairs = _state.value.selectedMatches[question.id]
                                ?.map { MatchingAnswer(it.key, it.value) }
                                .orEmpty()
                        )
                    }
                )
                submitLessonAttemptUseCase(attempt)
            }.onSuccess {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    result = it,
                    showResult = true
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = "Не удалось отправить результат урока."
                )
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                getQuestionsUseCase(lessonId)
            }.onSuccess { questions ->
                // Перемешиваем правые варианты у matching-вопросов один раз на загрузке,
                // чтобы правильные ответы не были на одной строке с левыми.
                val shuffled = questions.associate { q ->
                    q.id to if (q.type == "matching") {
                        var attempts = 0
                        var permuted: List<com.example.profdevelop.domain.model.MatchingPair>
                        do {
                            permuted = q.matchingPairs.shuffled()
                            attempts++
                            // На малом количестве пар возможна перестановка, повторяющая исходную —
                            // в этом случае пробуем ещё раз (но не уходим в бесконечность).
                        } while (
                            attempts < 5 &&
                            q.matchingPairs.size > 1 &&
                            permuted.zip(q.matchingPairs).all { it.first.id == it.second.id }
                        )
                        permuted
                    } else {
                        q.matchingPairs
                    }
                }
                _state.value = LessonUiState(
                    isLoading = false,
                    questions = questions,
                    shuffledRightOptions = shuffled
                )
            }.onFailure {
                _state.value = LessonUiState(
                    isLoading = false,
                    error = "Не удалось загрузить вопросы урока."
                )
            }
        }
    }
}

data class LessonUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Set<Int>> = emptyMap(),
    val selectedMatches: Map<Int, Map<Int, Int>> = emptyMap(),
    val shuffledRightOptions: Map<Int, List<com.example.profdevelop.domain.model.MatchingPair>> = emptyMap(),
    val showResult: Boolean = false,
    val result: LessonResult? = null,
    val error: String? = null
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentQuestionIndex)
    val canGoNext: Boolean get() = currentQuestionIndex < questions.lastIndex
    val canGoBack: Boolean get() = currentQuestionIndex > 0
    val isLastQuestion: Boolean get() = currentQuestionIndex == questions.lastIndex
}

class LessonViewModelFactory(
    private val lessonId: Int,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val submitLessonAttemptUseCase: SubmitLessonAttemptUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LessonViewModel(lessonId, getQuestionsUseCase, submitLessonAttemptUseCase) as T
    }
}
