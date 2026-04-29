package com.example.profdevelop.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandDanger
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandText
import com.example.profdevelop.presentation.theme.BrandWarmSoft

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(state.message) {
        val msg = state.message
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeMessage()
        }
    }

    // Перепланируем worker, если поменялся тумблер «Напоминание» или час.
    LaunchedEffect(state.settings.dailyReminderEnabled, state.settings.dailyReminderHour) {
        if (state.settings.dailyReminderEnabled) {
            com.example.profdevelop.notifications.StreakReminderWorker.schedule(
                context.applicationContext, state.settings.dailyReminderHour
            )
        } else {
            com.example.profdevelop.notifications.StreakReminderWorker.cancel(
                context.applicationContext
            )
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BrandBackground)
        .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = BrandText
                        )
                    }
                    Text(
                        text = "Настройки",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandText
                    )
                }
            }

            item {
                SectionHeader("Уведомления")
            }
            item {
                ToggleRow(
                    icon = Icons.Filled.Notifications,
                    title = "Ежедневное напоминание",
                    subtitle = "Напомним пройти урок и сохранить серию",
                    checked = state.settings.dailyReminderEnabled,
                    onChecked = viewModel::toggleReminder
                )
            }
            if (state.settings.dailyReminderEnabled) {
                item {
                    SettingsCard {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LeadingIcon(Icons.Filled.Bedtime, BrandWarmSoft, BrandGreen)
                                Spacer(Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Время напоминания",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BrandText
                                    )
                                    Text(
                                        text = "%02d:00".format(state.settings.dailyReminderHour),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BrandMuted
                                    )
                                }
                            }
                            Slider(
                                value = state.settings.dailyReminderHour.toFloat(),
                                onValueChange = { viewModel.setReminderHour(it.toInt()) },
                                valueRange = 6f..23f,
                                steps = 16,
                                colors = SliderDefaults.colors(
                                    thumbColor = BrandGreen,
                                    activeTrackColor = BrandGreen,
                                    inactiveTrackColor = BrandOutline
                                )
                            )
                        }
                    }
                }
            }

            item { SectionHeader("Цели") }
            item {
                SettingsCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LeadingIcon(Icons.Filled.Flag, BrandGreenSoft, BrandGreen)
                            Spacer(Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Дневная цель XP",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandText
                                )
                                Text(
                                    text = "${state.settings.dailyXpGoal} XP в день",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrandMuted
                                )
                            }
                        }
                        Slider(
                            value = state.settings.dailyXpGoal.toFloat(),
                            onValueChange = { viewModel.setDailyXpGoal(it.toInt()) },
                            valueRange = 10f..100f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = BrandGreen,
                                activeTrackColor = BrandGreen,
                                inactiveTrackColor = BrandOutline
                            )
                        )
                    }
                }
            }

            item { SectionHeader("Тактильность") }
            item {
                ToggleRow(
                    icon = Icons.Filled.Vibration,
                    title = "Вибрация",
                    subtitle = "Тактильный отклик на ответы и серии",
                    checked = state.settings.hapticsEnabled,
                    onChecked = viewModel::toggleHaptics
                )
            }

            item { SectionHeader("Внешний вид") }
            item {
                ToggleRow(
                    icon = Icons.Filled.TextFields,
                    title = "Крупный текст",
                    subtitle = "Увеличенный размер шрифта в уроках",
                    checked = state.settings.largeText,
                    onChecked = viewModel::toggleLargeText
                )
            }

            item { SectionHeader("Сервер") }
            item {
                SettingsCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LeadingIcon(Icons.Filled.Language, BrandWarmSoft, BrandGreen)
                            Spacer(Modifier.size(12.dp))
                            Text(
                                text = "Адрес API",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = state.apiUrl,
                            onValueChange = viewModel::updateApiUrl,
                            singleLine = true,
                            isError = state.apiUrlError != null,
                            supportingText = state.apiUrlError?.let {
                                { Text(it, color = BrandDanger) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = viewModel::saveApiUrl,
                            enabled = !state.isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = if (state.isSaving) "Сохраняем…" else "Сохранить",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = viewModel::resetAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Сбросить настройки", color = BrandDanger, fontWeight = FontWeight.Bold)
                }
            }

        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data -> Snackbar(snackbarData = data) }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = BrandMuted,
        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandOutline)
    ) {
        content()
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    SettingsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChecked(!checked) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LeadingIcon(icon, BrandGreenSoft, BrandGreen)
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandMuted
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onChecked,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BrandGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = BrandOutline
                )
            )
        }
    }
}

@Composable
private fun LeadingIcon(icon: ImageVector, bg: Color, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(bg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
    }
}
