package com.example.profdevelop.presentation.screens.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.usecase.GetLessonsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseViewModel(
    private val courseId: Int,
    private val getLessonsUseCase: GetLessonsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(CourseUiState())
    val state: StateFlow<CourseUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                getLessonsUseCase(courseId)
            }.onSuccess {
                _state.value = CourseUiState(isLoading = false, lessons = it)
            }.onFailure {
                _state.value = CourseUiState(
                    isLoading = false,
                    error = "Не удалось загрузить уроки курса."
                )
            }
        }
    }
}

data class CourseUiState(
    val isLoading: Boolean = true,
    val lessons: List<Lesson> = emptyList(),
    val error: String? = null
)

class CourseViewModelFactory(
    private val courseId: Int,
    private val getLessonsUseCase: GetLessonsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CourseViewModel(courseId, getLessonsUseCase) as T
    }
}
