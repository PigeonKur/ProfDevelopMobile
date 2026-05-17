package com.example.profdevelop.presentation.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onLogout: () -> Unit,
    onSelectTier: (String?) -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        state.user?.let {
            Text(it.fullName)
            it.positionTitle?.let { value -> Text(value) }
            Text("${it.totalXp}")
            Text("${it.level}")
            Text("${it.streakDays}")
        }
        if (state.leaderboardLoading) {
            CircularProgressIndicator(modifier = Modifier.testTag("leaderboard_loading"))
        }
        state.leaderboard.forEach { entry ->
            Text(entry.fullName)
        }
        Button(onClick = onLogout, modifier = Modifier.testTag("logout_button")) {
            Text("Выйти")
        }
    }
}
