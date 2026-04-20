package com.example.profdevelop.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.usecase.RestoreSessionUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val restoreSessionUseCase: RestoreSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            delay(1100)
            val session = restoreSessionUseCase()
            _state.value = if (session != null) SplashState.Authorized else SplashState.Unauthorized
        }
    }
}

sealed interface SplashState {
    data object Loading : SplashState
    data object Authorized : SplashState
    data object Unauthorized : SplashState
}

class SplashViewModelFactory(
    private val restoreSessionUseCase: RestoreSessionUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplashViewModel(restoreSessionUseCase) as T
    }
}
