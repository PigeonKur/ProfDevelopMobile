package com.example.profdevelop.presentation.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.profdevelop.domain.model.Achievement
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandDanger
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandText
import com.example.profdevelop.presentation.theme.BrandWarm
import com.example.profdevelop.presentation.theme.StreakOrange
import com.example.profdevelop.presentation.theme.XpGold

@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel,
    refreshToken: Int
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(refreshToken) {
        viewModel.refreshIfNeeded(refreshToken)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        Text(
            text = "Достижения",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = BrandText,
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, end = 20.dp)
        )

        val earned = state.items.count { it.isEarned }
        val total = state.items.size
        Text(
            text = if (total > 0) "Получено $earned из $total" else "Получай ачивки за прогресс — серии, XP, пройденные курсы",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandMuted,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 16.dp)
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = BrandGreen)
            }
            state.error != null -> Text(
                text = state.error!!,
                color = BrandDanger,
                modifier = Modifier.padding(20.dp)
            )
            state.items.isEmpty() -> EmptyAchievements()
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    AchievementCard(item)
                }
            }
        }
    }
}

private data class AchievementVisuals(
    val accent: Color,
    val accentSoft: Color
)

private fun visualsFor(item: Achievement): AchievementVisuals = when (item.conditionKey) {
    "streak_days" -> AchievementVisuals(StreakOrange, Color(0xFFFFE7C2))
    "total_xp" -> AchievementVisuals(XpGold, Color(0xFFFFF3C2))
    "courses_done" -> AchievementVisuals(BrandWarm, Color(0xFFEAF2FF))
    else -> AchievementVisuals(BrandGreen, BrandGreenSoft)
}

@Composable
private fun AchievementCard(item: Achievement) {
    val (accent, accentSoft) = visualsFor(item)
    val locked = !item.isEarned

    val cardBg = if (locked) BrandSurface else accentSoft
    val cardBorder = if (locked) BrandOutline else accent.copy(alpha = 0.4f)
    val titleColor = if (locked) BrandMuted else BrandText
    val descColor = if (locked) BrandMuted.copy(alpha = 0.85f) else BrandMuted
    val iconBg = if (locked) BrandOutline else accent
    val iconTint = if (locked) BrandMuted else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when {
                    item.isEarned -> AchievementGlyph(item, iconTint)
                    else -> Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Закрыто",
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            if (!item.description.isNullOrBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = descColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.weight(1f))

            if (item.isEarned) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Получено",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (item.hasProgress) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { item.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = accent,
                        trackColor = BrandOutline
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.currentValue ?: 0} / ${item.conditionValue ?: 0}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementGlyph(item: Achievement, tint: Color) {
    val custom = item.icon?.takeIf { it.isNotBlank() }
    if (custom != null && custom.length <= 3) {
        // эмодзи из БД
        Text(
            text = custom,
            style = MaterialTheme.typography.headlineSmall
        )
        return
    }
    val vector = when (item.conditionKey) {
        "streak_days" -> Icons.Filled.Star
        else -> Icons.Filled.EmojiEvents
    }
    Icon(
        imageVector = vector,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(32.dp)
    )
}

@Composable
private fun EmptyAchievements() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = null,
            tint = XpGold,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Достижений пока нет",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BrandText
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Проходи уроки, продлевай серию — и они появятся здесь.",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandMuted,
            textAlign = TextAlign.Center
        )
    }
}
