package com.example.profdevelop.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.usecase.GetAchievementsUseCase
import com.example.profdevelop.domain.usecase.GetAssignedCoursesUseCase
import com.example.profdevelop.domain.usecase.GetLessonsUseCase
import com.example.profdevelop.domain.usecase.GetStoredSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAssignedCoursesUseCase: GetAssignedCoursesUseCase,
    private val getLessonsUseCase: GetLessonsUseCase,
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase
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
                val session = getStoredSessionUseCase()
                val user = session?.user
                val courses = getAssignedCoursesUseCase().sortedBy { it.id }
                val chapters = courses.mapIndexed { index, course ->
                    HomeChapter(
                        index = index,
                        course = course,
                        lessons = getLessonsUseCase(course.id)
                    )
                }
                val achievementsCount = user?.id?.let { getAchievementsUseCase(it).size } ?: 0
                val nextLesson = chapters
                    .firstNotNullOfOrNull { chapter ->
                        chapter.lessons.firstOrNull { !it.isCompleted && it.isUnlocked }?.let { lesson ->
                            HomeNextLesson(chapter.course.id, chapter.course.title, lesson)
                        }
                    }

                HomeUiState(
                    isLoading = false,
                    user = user,
                    achievementsCount = achievementsCount,
                    chapters = chapters,
                    nextLesson = nextLesson
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

data class HomeChapter(
    val index: Int,
    val course: Course,
    val lessons: List<Lesson>
) {
    val completedCount: Int get() = lessons.count { it.isCompleted }
}

data class HomeNextLesson(
    val courseId: Int,
    val courseTitle: String,
    val lesson: Lesson
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: UserProfile? = null,
    val achievementsCount: Int = 0,
    val chapters: List<HomeChapter> = emptyList(),
    val nextLesson: HomeNextLesson? = null,
    val error: String? = null
)

class HomeViewModelFactory(
    private val getAssignedCoursesUseCase: GetAssignedCoursesUseCase,
    private val getLessonsUseCase: GetLessonsUseCase,
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            getAssignedCoursesUseCase,
            getLessonsUseCase,
            getStoredSessionUseCase,
            getAchievementsUseCase
        ) as T
    }
}
