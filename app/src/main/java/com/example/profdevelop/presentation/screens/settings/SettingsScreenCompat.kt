package com.example.profdevelop.presentation.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onApiUrlChange: (String) -> Unit,
    onSaveApiUrl: () -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onSetReminderHour: (Int) -> Unit,
    onToggleLargeText: (Boolean) -> Unit,
    onResetAll: () -> Unit,
    onConsumeMessage: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = state.apiUrl,
            onValueChange = onApiUrlChange,
            modifier = Modifier.testTag("api_url_field")
        )
        Button(onClick = onSaveApiUrl, modifier = Modifier.testTag("save_api_url_button")) {
            Text("Сохранить")
        }
        Checkbox(
            checked = state.settings.hapticsEnabled,
            onCheckedChange = onToggleHaptics,
            modifier = Modifier.testTag("haptics_toggle")
        )
        Checkbox(
            checked = state.reminderEnabled ?: state.settings.dailyReminderEnabled,
            onCheckedChange = onToggleReminder,
            modifier = Modifier.testTag("reminder_toggle")
        )
        Checkbox(
            checked = state.settings.largeText,
            onCheckedChange = onToggleLargeText,
            modifier = Modifier.testTag("large_text_toggle")
        )
        Button(onClick = onResetAll, modifier = Modifier.testTag("reset_button")) {
            Text("Сбросить")
        }
        if (state.reminderEnabled ?: state.settings.dailyReminderEnabled) {
            Slider(
                value = state.settings.dailyReminderHour.toFloat(),
                onValueChange = { onSetReminderHour(it.toInt()) },
                valueRange = 0f..23f,
                modifier = Modifier.testTag("reminder_hour_picker")
            )
        }
        state.apiUrlError?.let { Text(it) }
        state.message?.let { Text(it) }
    }
}
