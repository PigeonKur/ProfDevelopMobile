package com.example.profdevelop.presentation.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.profdevelop.R
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandDanger
import com.example.profdevelop.presentation.theme.BrandDangerSoft
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandText
import com.example.profdevelop.presentation.theme.BrandWarm
import com.example.profdevelop.presentation.theme.BrandWarmSoft

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLoggedOut: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val user = state.user

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandText,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Настройки",
                        tint = BrandText
                    )
                }
            }
        }

        if (user == null) {
            item { Text("Нет данных", color = BrandMuted) }
        } else {
            item { ProfileHeader(user) }
            item { StatsBlock(user) }
            item { LevelProgress(user) }
            item {
                LeaderboardSection(
                    currentUserId = user.id,
                    entries = state.leaderboard,
                    isLoading = state.leaderboardLoading,
                    error = state.leaderboardError,
                    onRetry = { viewModel.loadLeaderboard() }
                )
            }
            item {
                OutlinedButton(
                    onClick = { viewModel.logout(onLoggedOut) },
                    enabled = !state.isLoggingOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (state.isLoggingOut) "Выходим..." else "Выйти из аккаунта",
                        color = BrandDanger,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandOutline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(BrandGreen, Color(0xFF2E6A3A))
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.fullName.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD4A000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${user.level}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        val sub = listOfNotNull(user.positionTitle, user.departmentName)
                            .joinToString(" • ")
                        if (sub.isNotBlank()) {
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsBlock(user: UserProfile) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.xp,
            iconTint = null,
            value = "${user.totalXp}",
            label = "Всего XP",
            background = Color(0xFFFFF9E0)
        )
        StatChip(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.burn,
            iconTint = if (user.isStreakActive()) null else Color(0xFF9DA89E),
            value = "${user.streakDays}",
            label = "Серия",
            background = if (user.isStreakActive()) Color(0xFFFFF3E0) else Color(0xFFEFEFEF)
        )
        StatChip(
            modifier = Modifier.weight(1f),
            iconRes = R.drawable.approved,
            iconTint = null,
            value = "${user.level}",
            label = "Уровень",
            background = BrandGreenSoft
        )
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    iconRes: Int,
    iconTint: Color?,
    value: String,
    label: String,
    background: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    colorFilter = iconTint?.let { ColorFilter.tint(it) }
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandText
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = BrandMuted
            )
        }
    }
}

@Composable
private fun LevelProgress(user: UserProfile) {
    val xpInLevel = user.totalXp % 100
    val progress = xpInLevel / 100f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandOutline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Уровень ${user.level}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandText
                )
                Text(
                    text = "$xpInLevel / 100 XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandMuted
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = BrandGreen,
                trackColor = BrandGreenSoft
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "До следующего уровня осталось ${100 - xpInLevel} XP",
                style = MaterialTheme.typography.bodySmall,
                color = BrandMuted
            )
        }
    }
}

private fun UserProfile.isStreakActive(): Boolean {
    val date = lastActiveDate ?: return false
    return runCatching {
        java.time.LocalDate.parse(date.take(10)) == java.time.LocalDate.now()
    }.getOrDefault(false)
}

@Composable
private fun LeaderboardSection(
    currentUserId: Int,
    entries: List<com.example.profdevelop.domain.model.LeaderboardEntry>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Лидерборд",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = BrandText,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Кто впереди",
                style = MaterialTheme.typography.labelSmall,
                color = BrandMuted
            )
        }

        when {
            isLoading && entries.isEmpty() -> Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurface)
            ) {
                Text(
                    text = "Загружаем...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    color = BrandMuted
                )
            }
            error != null && entries.isEmpty() -> Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandDangerSoft)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Не удалось загрузить лидерборд",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandDanger,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Сравним прогресс с коллегами, как только появится сеть.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandMuted
                    )
                    OutlinedButton(onClick = onRetry) {
                        Text("Повторить", color = BrandDanger, fontWeight = FontWeight.Bold)
                    }
                }
            }
            entries.isEmpty() -> Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurface)
            ) {
                Text(
                    text = "Пока никого нет — будь первым!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    color = BrandMuted
                )
            }
            else -> Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                entries.take(10).forEach { entry ->
                    LeaderboardRow(entry = entry, isMe = entry.userId == currentUserId)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: com.example.profdevelop.domain.model.LeaderboardEntry,
    isMe: Boolean
) {
    val containerColor = when {
        isMe -> BrandGreenSoft
        entry.rank == 1 -> Color(0xFFFFF4D6)
        else -> BrandSurface
    }
    val rankBg = when (entry.rank) {
        1 -> Color(0xFFD4A000)
        2 -> Color(0xFF9DA89E)
        3 -> Color(0xFFCD7F32)
        else -> BrandOutline
    }
    val rankText = if (entry.rank == 0) "—" else "#${entry.rank}"
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(rankBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rankText,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = entry.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandText,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isMe) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(BrandGreen)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ВЫ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                if (!entry.positionTitle.isNullOrBlank()) {
                    Text(
                        text = entry.positionTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandMuted,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.xp),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${entry.totalXp}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandText
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.burn),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        colorFilter = if (entry.streakDays > 0) null
                            else ColorFilter.tint(Color(0xFF9DA89E))
                    )
                    Text(
                        text = "${entry.streakDays}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
