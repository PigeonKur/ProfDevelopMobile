package com.example.profdevelop.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.profdevelop.data.local.AppSettings
import com.example.profdevelop.data.local.SettingsPreferencesDataSource
import com.example.profdevelop.domain.usecase.GetApiUrlUseCase
import com.example.profdevelop.domain.usecase.UpdateApiUrlUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val apiUrl: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val apiUrlError: String? = null
)

private val URL_REGEX = Regex("^https?://[^\\s]+\\S$")

class SettingsViewModel(
    private val dataSource: SettingsPreferencesDataSource,
    private val getApiUrlUseCase: GetApiUrlUseCase,
    private val updateApiUrlUseCase: UpdateApiUrlUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            dataSource.flow.collect { settings ->
                _state.update { it.copy(settings = settings) }
            }
        }
        viewModelScope.launch {
            val url = getApiUrlUseCase()
            _state.update { it.copy(apiUrl = url) }
        }
    }

    fun toggleHaptics(value: Boolean) = viewModelScope.launch { dataSource.setHaptics(value) }
    fun toggleReminder(value: Boolean) = viewModelScope.launch { dataSource.setReminderEnabled(value) }
    fun setReminderHour(value: Int) = viewModelScope.launch { dataSource.setReminderHour(value) }
    fun setDailyXpGoal(value: Int) = viewModelScope.launch { dataSource.setDailyXpGoal(value) }
    fun toggleLargeText(value: Boolean) = viewModelScope.launch { dataSource.setLargeText(value) }
    fun toggleAnalytics(value: Boolean) = viewModelScope.launch { dataSource.setAnalytics(value) }

    fun updateApiUrl(url: String) {
        _state.update { it.copy(apiUrl = url, apiUrlError = null) }
    }

    fun saveApiUrl() {
        val raw = state.value.apiUrl.trim()
        if (!URL_REGEX.matches(raw)) {
            _state.update {
                it.copy(apiUrlError = "Адрес должен начинаться с http:// или https:// и не содержать пробелов")
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, apiUrlError = null) }
            updateApiUrlUseCase(raw)
            val saved = getApiUrlUseCase()
            _state.update {
                it.copy(
                    isSaving = false,
                    apiUrl = saved,
                    message = "Адрес сервера сохранён"
                )
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun resetAll() {
        viewModelScope.launch {
            dataSource.resetAll()
            _state.update { it.copy(message = "Настройки сброшены") }
        }
    }

    class Factory(
        private val dataSource: SettingsPreferencesDataSource,
        private val getApiUrlUseCase: GetApiUrlUseCase,
        private val updateApiUrlUseCase: UpdateApiUrlUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(dataSource, getApiUrlUseCase, updateApiUrlUseCase) as T
        }
    }
}
