package com.example.profdevelop.presentation.screens.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.model.LessonAttempt
import com.example.profdevelop.domain.model.LessonResult
import com.example.profdevelop.domain.model.MatchingAnswer
import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.domain.model.QuestionAttempt
import com.example.profdevelop.domain.usecase.CheckQuestionUseCase
import com.example.profdevelop.domain.usecase.GetQuestionsUseCase
import com.example.profdevelop.domain.usecase.SubmitLessonAttemptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LessonViewModel(
    private val lessonId: Int,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val checkQuestionUseCase: CheckQuestionUseCase,
    private val submitLessonAttemptUseCase: SubmitLessonAttemptUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(LessonUiState())
    val state: StateFlow<LessonUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun selectAnswer(questionId: Int, answerId: Int) {
        if (_state.value.feedback != null) return

        val question = _state.value.questions.firstOrNull { it.id == questionId } ?: return
        val updated = _state.value.selectedAnswers.toMutableMap()
        val current = updated[questionId].orEmpty().toMutableSet()

        if (question.type == "choice") {
            if (!current.add(answerId)) current.remove(answerId)
            updated[questionId] = current
        } else {
            updated[questionId] = setOf(answerId)
        }

        _state.value = _state.value.copy(selectedAnswers = updated, error = null)
    }

    fun selectMatching(questionId: Int, leftPairId: Int, rightPairId: Int) {
        if (_state.value.feedback != null) return

        val updated = _state.value.selectedMatches.toMutableMap()
        val questionMap = updated[questionId]?.toMutableMap() ?: mutableMapOf()

        val alreadyUsedBy = questionMap.entries.firstOrNull { it.value == rightPairId }?.key
        if (alreadyUsedBy != null && alreadyUsedBy != leftPairId) {
            questionMap.remove(alreadyUsedBy)
        }

        questionMap[leftPairId] = rightPairId
        updated[questionId] = questionMap
        _state.value = _state.value.copy(selectedMatches = updated, error = null)
    }

    fun checkCurrentQuestion() {
        val question = _state.value.currentQuestion ?: return
        val attempt = buildAttempt(question)

        if (!isAnswerComplete(question, attempt)) {
            _state.value = _state.value.copy(error = "Сначала выберите ответ.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isChecking = true, error = null)
            runCatching {
                checkQuestionUseCase(attempt)
            }.onSuccess { review ->
                val updatedQueue = if (review.isCorrect) {
                    _state.value.questionQueue
                } else {
                    _state.value.questionQueue + question.id
                }
                val updatedCompleted = if (review.isCorrect) {
                    _state.value.completedQuestionIds + question.id
                } else {
                    _state.value.completedQuestionIds
                }

                _state.value = _state.value.copy(
                    isChecking = false,
                    questionQueue = updatedQueue,
                    completedQuestionIds = updatedCompleted,
                    feedback = LessonFeedback(
                        isCorrect = review.isCorrect,
                        message = review.explanation?.takeIf { it.isNotBlank() }
                            ?: if (review.isCorrect) "Верно. Можно идти дальше." else "Ответ неверный. Этот вопрос вернётся в конец урока."
                    )
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isChecking = false,
                    error = "Не удалось проверить ответ."
                )
            }
        }
    }

    fun continueAfterFeedback() {
        val feedback = _state.value.feedback ?: return
        val nextIndex = _state.value.currentQuestionIndex + 1

        if (feedback.isCorrect && nextIndex > _state.value.questionQueue.lastIndex) {
            submitFinalLesson()
            return
        }

        _state.value = _state.value.copy(
            currentQuestionIndex = nextIndex.coerceAtMost(_state.value.questionQueue.lastIndex),
            feedback = null,
            error = null
        )
    }

    fun previousQuestion() {
        if (_state.value.feedback != null) return
        val prev = (_state.value.currentQuestionIndex - 1).coerceAtLeast(0)
        _state.value = _state.value.copy(currentQuestionIndex = prev)
    }

    private fun submitFinalLesson() {
        val questions = _state.value.questions
        if (questions.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSubmitting = true, error = null)
            runCatching {
                val attempt = LessonAttempt(
                    lessonId = lessonId,
                    answers = questions.map { buildAttempt(it) }
                )
                submitLessonAttemptUseCase(attempt)
            }.onSuccess {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    feedback = null,
                    result = it,
                    showResult = true
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    error = "Не удалось завершить урок."
                )
            }
        }
    }

    private fun buildAttempt(question: Question): QuestionAttempt =
        QuestionAttempt(
            questionId = question.id,
            selectedAnswerIds = _state.value.selectedAnswers[question.id]?.toList().orEmpty(),
            matchingPairs = _state.value.selectedMatches[question.id]
                ?.map { MatchingAnswer(it.key, it.value) }
                .orEmpty()
        )

    private fun isAnswerComplete(question: Question, attempt: QuestionAttempt): Boolean =
        when (question.type) {
            "choice", "truefalse" -> attempt.selectedAnswerIds.isNotEmpty()
            "matching" -> attempt.matchingPairs.size == question.matchingPairs.size
            else -> false
        }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                getQuestionsUseCase(lessonId)
            }.onSuccess { questions ->
                _state.value = LessonUiState(
                    isLoading = false,
                    questions = questions,
                    questionQueue = questions.map { it.id }
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

data class LessonFeedback(
    val isCorrect: Boolean,
    val message: String
)

data class LessonUiState(
    val isLoading: Boolean = true,
    val isChecking: Boolean = false,
    val isSubmitting: Boolean = false,
    val questions: List<Question> = emptyList(),
    val questionQueue: List<Int> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Set<Int>> = emptyMap(),
    val selectedMatches: Map<Int, Map<Int, Int>> = emptyMap(),
    val completedQuestionIds: Set<Int> = emptySet(),
    val feedback: LessonFeedback? = null,
    val showResult: Boolean = false,
    val result: LessonResult? = null,
    val error: String? = null
) {
    val currentQuestion: Question?
        get() = questions.firstOrNull { it.id == questionQueue.getOrNull(currentQuestionIndex) }

    val canGoBack: Boolean get() = currentQuestionIndex > 0
    val progressLabel: String get() = "${completedQuestionIds.size} из ${questions.size}"
}

class LessonViewModelFactory(
    private val lessonId: Int,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val checkQuestionUseCase: CheckQuestionUseCase,
    private val submitLessonAttemptUseCase: SubmitLessonAttemptUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LessonViewModel(
            lessonId = lessonId,
            getQuestionsUseCase = getQuestionsUseCase,
            checkQuestionUseCase = checkQuestionUseCase,
            submitLessonAttemptUseCase = submitLessonAttemptUseCase
        ) as T
    }
}
