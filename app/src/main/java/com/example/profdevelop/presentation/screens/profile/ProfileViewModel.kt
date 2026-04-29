package com.example.profdevelop.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.model.LeaderboardEntry
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.usecase.GetLeaderboardUseCase
import com.example.profdevelop.domain.usecase.GetStoredSessionUseCase
import com.example.profdevelop.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        refreshSession()
        loadLeaderboard()
    }

    fun refreshSession() {
        viewModelScope.launch {
            val session = getStoredSessionUseCase()
            _state.value = _state.value.copy(user = session?.user)
        }
    }

    fun refresh() {
        refreshSession()
        loadLeaderboard()
    }

    fun loadLeaderboard(tier: String? = _state.value.tierFilter) {
        viewModelScope.launch {
            _state.value = _state.value.copy(leaderboardLoading = true, tierFilter = tier)
            runCatching { getLeaderboardUseCase(tier) }
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        leaderboard = list,
                        leaderboardLoading = false,
                        leaderboardError = null
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        leaderboardLoading = false,
                        leaderboardError = e.message ?: "Не удалось загрузить лидерборд"
                    )
                }
        }
    }

    fun selectTier(tier: String?) {
        loadLeaderboard(tier)
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoggingOut = true)
            runCatching { logoutUseCase() }
            onDone()
        }
    }
}

data class ProfileUiState(
    val user: UserProfile? = null,
    val isLoggingOut: Boolean = false,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val leaderboardLoading: Boolean = false,
    val leaderboardError: String? = null,
    val tierFilter: String? = null
)

class ProfileViewModelFactory(
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProfileViewModel(getStoredSessionUseCase, logoutUseCase, getLeaderboardUseCase) as T
}
