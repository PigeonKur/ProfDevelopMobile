package com.example.profdevelop.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.domain.usecase.GetApiUrlUseCase
import com.example.profdevelop.domain.usecase.LoginUseCase
import com.example.profdevelop.domain.usecase.UpdateApiUrlUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val getApiUrlUseCase: GetApiUrlUseCase,
    private val updateApiUrlUseCase: UpdateApiUrlUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        loadApiUrl()
    }

    fun updateEmail(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun updatePassword(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    fun toggleRememberMe() {
        _state.value = _state.value.copy(rememberMe = !_state.value.rememberMe)
    }

    fun toggleServerSettings() {
        _state.value = _state.value.copy(showServerSettings = !_state.value.showServerSettings)
    }

    fun updateApiUrl(value: String) {
        _state.value = _state.value.copy(apiUrl = value, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        val current = _state.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _state.value = current.copy(error = "Введите email и пароль.")
            return
        }

        if (current.apiUrl.isBlank()) {
            _state.value = current.copy(error = "Укажите адрес API.")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, error = null)
            updateApiUrlUseCase(current.apiUrl)

            val result = loginUseCase(
                email = current.email,
                password = current.password,
                rememberMe = current.rememberMe
            )

            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось войти. Проверьте данные и адрес API."
                )
            }
        }
    }

    private fun loadApiUrl() {
        viewModelScope.launch {
            _state.value = _state.value.copy(apiUrl = getApiUrlUseCase())
        }
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = true,
    val apiUrl: String = "",
    val showServerSettings: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModelFactory(
    private val loginUseCase: LoginUseCase,
    private val getApiUrlUseCase: GetApiUrlUseCase,
    private val updateApiUrlUseCase: UpdateApiUrlUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(
            loginUseCase = loginUseCase,
            getApiUrlUseCase = getApiUrlUseCase,
            updateApiUrlUseCase = updateApiUrlUseCase
        ) as T
    }
}
