package com.example.profdevelop.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.data.local.SettingsPreferencesDataSource
import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.usecase.ActivateXpBoostUseCase
import com.example.profdevelop.domain.usecase.GetAchievementsUseCase
import com.example.profdevelop.domain.usecase.GetAssignedCoursesUseCase
import com.example.profdevelop.domain.usecase.GetLessonsUseCase
import com.example.profdevelop.domain.usecase.GetStoredSessionUseCase
import com.example.profdevelop.domain.usecase.GetXpBoostStatusUseCase
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getAssignedCoursesUseCase: GetAssignedCoursesUseCase,
    private val getLessonsUseCase: GetLessonsUseCase,
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val getXpBoostStatusUseCase: GetXpBoostStatusUseCase,
    private val activateXpBoostUseCase: ActivateXpBoostUseCase,
    private val settingsDataSource: SettingsPreferencesDataSource
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var boostTickerJob: Job? = null
    private var activatingBoost = false

    init {
        viewModelScope.launch {
            settingsDataSource.flow.collect { settings ->
                _state.update {
                    it.copy(
                        dailyXpGoal = settings.dailyXpGoal,
                        lastGoalCelebrateDate = settings.lastDailyGoalCelebrateDate
                    )
                }
                refreshBoostStatus()
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val session = getStoredSessionUseCase()
                val user = session?.user
                val courses = getAssignedCoursesUseCase().sortedBy { it.id }

                val (chapters, achievementsCount) = coroutineScope {
                    val lessonsDeferred = courses.mapIndexed { index, course ->
                        async {
                            HomeChapter(
                                index = index,
                                course = course,
                                lessons = getLessonsUseCase(course.id)
                            )
                        }
                    }
                    val achievementsDeferred = async {
                        user?.id?.let { getAchievementsUseCase(it).size } ?: 0
                    }
                    lessonsDeferred.awaitAll() to achievementsDeferred.await()
                }

                val nextLesson = chapters.firstNotNullOfOrNull { chapter ->
                    chapter.lessons.firstOrNull { !it.isCompleted && it.isUnlocked }?.let { lesson ->
                        HomeNextLesson(chapter.course.id, chapter.course.title, lesson)
                    }
                }

                HomeUiState(
                    isLoading = false,
                    user = user,
                    achievementsCount = achievementsCount,
                    chapters = chapters,
                    nextLesson = nextLesson,
                    boostSecondsLeft = _state.value.boostSecondsLeft,
                    dailyXpGoal = _state.value.dailyXpGoal,
                    lastGoalCelebrateDate = _state.value.lastGoalCelebrateDate,
                    goalReachedToday = _state.value.goalReachedToday
                )
            }.onSuccess {
                _state.value = it
                refreshBoostStatus()
            }.onFailure {
                _state.value = HomeUiState(
                    isLoading = false,
                    error = "Не удалось загрузить учебный путь.",
                    dailyXpGoal = _state.value.dailyXpGoal,
                    lastGoalCelebrateDate = _state.value.lastGoalCelebrateDate,
                    goalReachedToday = _state.value.goalReachedToday
                )
            }
        }
    }

    fun activateBoost() {
        if (activatingBoost || _state.value.boostSecondsLeft > 0) return
        if (!_state.value.boostEligible) {
            _state.update {
                it.copy(boostMessage = "Сначала пройди 3 урока или набери ${it.dailyXpGoal} XP сегодня.")
            }
            return
        }

        activatingBoost = true
        _state.update { it.copy(boostActivating = true) }
        viewModelScope.launch {
            runCatching { activateXpBoostUseCase(dailyXpGoal = _state.value.dailyXpGoal) }
                .onSuccess { status ->
                    startBoostTicker(status.activeUntil, status.remainingSeconds.coerceAtLeast(0))
                    _state.update {
                        it.copy(
                            boostMessage = "2x XP включён на 30 минут!",
                            boostLessonsToday = status.lessonsToday,
                            boostXpToday = status.xpToday,
                            boostEligible = status.isEligible,
                            goalReachedToday = status.xpToday >= it.dailyXpGoal
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(boostMessage = "Не удалось включить буст. Попробуй позже.") }
                }
            _state.update { it.copy(boostActivating = false) }
            activatingBoost = false
        }
    }

    fun consumeBoostMessage() {
        _state.update { it.copy(boostMessage = null) }
    }

    private fun refreshBoostStatus() {
        viewModelScope.launch {
            runCatching { getXpBoostStatusUseCase(_state.value.dailyXpGoal) }
                .onSuccess { status ->
                    if (status.isActive) startBoostTicker(status.activeUntil, status.remainingSeconds.coerceAtLeast(0))
                    else stopBoostTicker()

                    val today = LocalDate.now().toString()
                    val shouldCelebrate =
                        status.xpToday >= _state.value.dailyXpGoal &&
                            _state.value.lastGoalCelebrateDate != today

                    if (shouldCelebrate) {
                        settingsDataSource.setLastDailyGoalCelebrateDate(today)
                    }

                    _state.update {
                        it.copy(
                            boostLessonsToday = status.lessonsToday,
                            boostXpToday = status.xpToday,
                            boostEligible = status.isEligible,
                            goalReachedToday = status.xpToday >= it.dailyXpGoal,
                            lastGoalCelebrateDate = if (shouldCelebrate) today else it.lastGoalCelebrateDate,
                            boostMessage = if (shouldCelebrate) {
                                "Поздравляем! Вы достигли дневной цели!"
                            } else it.boostMessage
                        )
                    }
                }
        }
    }

    private fun startBoostTicker(activeUntil: String?, initialSeconds: Int) {
        boostTickerJob?.cancel()
        _state.update { it.copy(boostSecondsLeft = initialSeconds) }
        if (initialSeconds <= 0) return

        val deadline = runCatching {
            activeUntil?.let { java.time.OffsetDateTime.parse(it).toInstant() }
        }.getOrNull()

        boostTickerJob = viewModelScope.launch {
            while (true) {
                val left = deadline
                    ?.let { java.time.Duration.between(java.time.Instant.now(), it).seconds.toInt() }
                    ?: _state.value.boostSecondsLeft

                val normalized = left.coerceAtLeast(0)
                _state.update { it.copy(boostSecondsLeft = normalized) }
                if (normalized <= 0) break
                delay(1000L)
            }
        }
    }

    private fun stopBoostTicker() {
        boostTickerJob?.cancel()
        boostTickerJob = null
        _state.update { it.copy(boostSecondsLeft = 0) }
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
    val error: String? = null,
    val boostSecondsLeft: Int = 0,
    val boostMessage: String? = null,
    val boostActivating: Boolean = false,
    val boostLessonsToday: Int = 0,
    val boostXpToday: Int = 0,
    val boostEligible: Boolean = false,
    val dailyXpGoal: Int = 30,
    val goalReachedToday: Boolean = false,
    val lastGoalCelebrateDate: String? = null
) {
    val boostActive: Boolean get() = boostSecondsLeft > 0
    val showBoostBanner: Boolean get() = boostActive || boostEligible
}

class HomeViewModelFactory(
    private val getAssignedCoursesUseCase: GetAssignedCoursesUseCase,
    private val getLessonsUseCase: GetLessonsUseCase,
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val getXpBoostStatusUseCase: GetXpBoostStatusUseCase,
    private val activateXpBoostUseCase: ActivateXpBoostUseCase,
    private val settingsDataSource: SettingsPreferencesDataSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            getAssignedCoursesUseCase,
            getLessonsUseCase,
            getStoredSessionUseCase,
            getAchievementsUseCase,
            getXpBoostStatusUseCase,
            activateXpBoostUseCase,
            settingsDataSource
        ) as T
    }
}