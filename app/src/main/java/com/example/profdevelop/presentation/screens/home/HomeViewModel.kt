package com.example.profdevelop.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.usecase.GetAssignedCoursesUseCase
import com.example.profdevelop.domain.usecase.GetLessonsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAssignedCoursesUseCase: GetAssignedCoursesUseCase,
    private val getLessonsUseCase: GetLessonsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching {
                val courses = getAssignedCoursesUseCase()
                val activeCourse = courses
                    .sortedWith(compareByDescending<Course> { it.progressPercent < 100 }.thenByDescending { it.progressPercent })
                    .firstOrNull()
                val lessons = activeCourse?.let { getLessonsUseCase(it.id) }.orEmpty()
                HomeUiState(
                    isLoading = false,
                    courses = courses,
                    activeCourse = activeCourse,
                    activeCourseLessons = lessons
                )
            }.onSuccess {
                _state.value = it
            }.onFailure {
                _state.value = HomeUiState(
                    isLoading = false,
                    error = "Не удалось загрузить учебный путь."
                )
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val courses: List<Course> = emptyList(),
    val activeCourse: Course? = null,
    val activeCourseLessons: List<Lesson> = emptyList(),
    val error: String? = null
)

class HomeViewModelFactory(
    private val getAssignedCoursesUseCase: GetAssignedCoursesUseCase,
    private val getLessonsUseCase: GetLessonsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(getAssignedCoursesUseCase, getLessonsUseCase) as T
    }
}
