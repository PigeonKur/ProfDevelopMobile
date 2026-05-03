package com.example.profdevelop.presentation.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.model.Achievement
import com.example.profdevelop.domain.usecase.GetAchievementsUseCase
import com.example.profdevelop.domain.usecase.GetStoredSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AchievementsViewModel(
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AchievementsUiState(isLoading = true))
    val state: StateFlow<AchievementsUiState> = _state.asStateFlow()

    // Перезагружаем список ачивок при первом открытии экрана и каждый раз,
    // когда родительский refreshToken изменился (например, после прохождения
    // урока или открытия новой ачивки).
    private var lastRefreshToken: Int = -1

    fun refreshIfNeeded(token: Int) {
        if (token != lastRefreshToken) {
            lastRefreshToken = token
            load()
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = AchievementsUiState(isLoading = true)
            runCatching {
                val session = getStoredSessionUseCase()
                    ?: error("Нет активной сессии")
                getAchievementsUseCase(session.user.id)
            }.onSuccess {
                _state.value = AchievementsUiState(
                    isLoading = false,
                    items = it
                )
            }.onFailure {
                _state.value = AchievementsUiState(
                    isLoading = false,
                    error = "Не удалось загрузить достижения."
                )
            }
        }
    }
}

data class AchievementsUiState(
    val isLoading: Boolean = false,
    val items: List<Achievement> = emptyList(),
    val error: String? = null
)

class AchievementsViewModelFactory(
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AchievementsViewModel(getStoredSessionUseCase, getAchievementsUseCase) as T
}
