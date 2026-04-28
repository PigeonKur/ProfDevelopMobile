package com.example.profdevelop.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.data.local.AuthPreferencesDataSource
import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.usecase.GetAssignedCoursesUseCase
import com.example.profdevelop.domain.usecase.GetLessonsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAssignedCoursesUseCase: GetAssignedCoursesUseCase,
    private val getLessonsUseCase: GetLessonsUseCase,
    private val authPreferences: AuthPreferencesDataSource
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
                val profile = authPreferences.getStoredSession()?.user
                val courses = getAssignedCoursesUseCase()
                val activeCourse = pickActiveCourse(courses)
                val mandatory = courses
                    .filter { it.isMandatory && it.id != activeCourse?.id }
                    .sortedWith(compareBy<Course, String?>(nullsLast()) { it.deadline })
                val lessons = activeCourse?.let { getLessonsUseCase(it.id) }.orEmpty()
                HomeUiState(
                    isLoading = false,
                    profile = profile,
                    courses = courses,
                    activeCourse = activeCourse,
                    mandatoryCourses = mandatory,
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

    private fun pickActiveCourse(courses: List<Course>): Course? {
        val started = courses.filter { it.completedLessons in 1 until it.totalLessons }
            .maxByOrNull { it.progressPercent }
        if (started != null) return started
        return courses
            .filter { it.progressPercent < 100 }
            .sortedWith(
                compareByDescending<Course> { it.isMandatory }
                    .thenBy(nullsLast()) { it.deadline }
            )
            .firstOrNull()
            ?: courses.firstOrNull()
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val courses: List<Course> = emptyList(),
    val activeCourse: Course? = null,
    val mandatoryCourses: List<Course> = emptyList(),
    val activeCourseLessons: List<Lesson> = emptyList(),
    val error: String? = null
)

class HomeViewModelFactory(
    private val getAssignedCoursesUseCase: GetAssignedCoursesUseCase,
    private val getLessonsUseCase: GetLessonsUseCase,
    private val authPreferences: AuthPreferencesDataSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(getAssignedCoursesUseCase, getLessonsUseCase, authPreferences) as T
    }
}
