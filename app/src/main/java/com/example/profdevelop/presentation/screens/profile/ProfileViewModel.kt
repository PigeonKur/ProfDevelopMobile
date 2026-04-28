package com.example.profdevelop.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.usecase.GetStoredSessionUseCase
import com.example.profdevelop.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val session = getStoredSessionUseCase()
            _state.value = _state.value.copy(user = session?.user)
        }
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
    val isLoggingOut: Boolean = false
)

class ProfileViewModelFactory(
    private val getStoredSessionUseCase: GetStoredSessionUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProfileViewModel(getStoredSessionUseCase, logoutUseCase) as T
}
