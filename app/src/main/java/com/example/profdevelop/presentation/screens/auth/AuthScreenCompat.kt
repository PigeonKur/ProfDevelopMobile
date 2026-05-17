package com.example.profdevelop.presentation.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleRemember: () -> Unit,
    onLogin: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_field")
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_field")
        )
        Checkbox(
            checked = state.rememberMe,
            onCheckedChange = { onToggleRemember() },
            modifier = Modifier.testTag("remember_me_checkbox")
        )
        state.error?.let {
            Text(text = it, modifier = Modifier.testTag("error_text"))
        }
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
        } else {
            Button(
                onClick = onLogin,
                enabled = state.email.isNotBlank() && state.password.isNotBlank(),
                modifier = Modifier.testTag("login_button")
            ) {
                Text("Войти")
            }
        }
    }
}
