package com.example.profdevelop.presentation.screens.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.domain.model.QuestionAttempt
import com.example.profdevelop.domain.usecase.CheckQuestionUseCase
import com.example.profdevelop.domain.usecase.GetPracticeQuestionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(
    private val getPracticeQuestionsUseCase: GetPracticeQuestionsUseCase,
    private val checkQuestionUseCase: CheckQuestionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    // Помним последний refreshToken, чтобы не перегружать список при простом
    // переключении вкладок. Загружаем заново только если экран открыли впервые
    // или после события (например, прохождения урока), которое инкрементнуло токен.
    private var lastRefreshToken: Int = -1

    fun refreshIfNeeded(token: Int) {
        if (token != lastRefreshToken) {
            lastRefreshToken = token
            load()
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = PracticeUiState(isLoading = true)
            runCatching {
                getPracticeQuestionsUseCase(limit = 12)
            }.onSuccess { qs ->
                // Тренировка: убираем matching ради простого UI повторения.
                val filtered = qs.filter { it.type == "choice" || it.type == "truefalse" }
                _state.value = PracticeUiState(
                    isLoading = false,
                    questions = filtered
                )
            }.onFailure {
                _state.value = PracticeUiState(
                    isLoading = false,
                    error = "Не удалось загрузить вопросы для практики."
                )
            }
        }
    }

    fun selectAnswer(answerId: Int) {
        if (_state.value.feedback != null) return
        val current = _state.value.currentQuestion ?: return
        val selected = _state.value.selected.toMutableSet()
        if (current.type == "choice") {
            if (!selected.add(answerId)) selected.remove(answerId)
        } else {
            selected.clear()
            selected.add(answerId)
        }
        _state.value = _state.value.copy(selected = selected, error = null)
    }

    fun check() {
        val current = _state.value.currentQuestion ?: return
        if (_state.value.selected.isEmpty()) {
            _state.value = _state.value.copy(error = "Сначала выбери ответ.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isChecking = true, error = null)
            runCatching {
                checkQuestionUseCase(
                    QuestionAttempt(
                        questionId = current.id,
                        selectedAnswerIds = _state.value.selected.toList(),
                        matchingPairs = emptyList()
                    )
                )
            }.onSuccess { review ->
                _state.value = _state.value.copy(
                    isChecking = false,
                    feedback = PracticeFeedback(
                        isCorrect = review.isCorrect,
                        explanation = review.explanation
                    ),
                    correctCount = if (review.isCorrect) _state.value.correctCount + 1 else _state.value.correctCount
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isChecking = false,
                    error = "Не удалось проверить ответ."
                )
            }
        }
    }

    fun next() {
        val s = _state.value
        val nextIndex = s.currentIndex + 1
        if (nextIndex > s.questions.lastIndex) {
            _state.value = s.copy(finished = true)
        } else {
            _state.value = s.copy(
                currentIndex = nextIndex,
                selected = emptySet(),
                feedback = null,
                error = null
            )
        }
    }
}

data class PracticeFeedback(
    val isCorrect: Boolean,
    val explanation: String?
)

data class PracticeUiState(
    val isLoading: Boolean = false,
    val isChecking: Boolean = false,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selected: Set<Int> = emptySet(),
    val feedback: PracticeFeedback? = null,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val error: String? = null
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val total: Int get() = questions.size
}

class PracticeViewModelFactory(
    private val getPracticeQuestionsUseCase: GetPracticeQuestionsUseCase,
    private val checkQuestionUseCase: CheckQuestionUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PracticeViewModel(getPracticeQuestionsUseCase, checkQuestionUseCase) as T
}
