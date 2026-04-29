package com.example.profdevelop.presentation.screens.quests

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import com.example.profdevelop.domain.model.XpBoostStatus
import com.example.profdevelop.domain.usecase.GetStoredSessionUseCase
import com.example.profdevelop.domain.usecase.GetXpBoostStatusUseCase
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandText
import com.example.profdevelop.presentation.theme.BrandWarmSoft

@Composable
fun QuestsScreen(
    getStoredSessionUseCase: GetStoredSessionUseCase,
    getXpBoostStatusUseCase: GetXpBoostStatusUseCase
) {
    val user by produceState<UserProfile?>(initialValue = null) {
        value = getStoredSessionUseCase()?.user
    }

    // Прогресс дневных заданий считается на сервере (boost-status даёт
    // lessonsToday и xpToday, посчитанные с 00:00 UTC).
    val daily by produceState<XpBoostStatus?>(initialValue = null, user) {
        if (user == null) return@produceState
        runCatching { getXpBoostStatusUseCase() }
            .onSuccess { value = it }
    }

    val items = remember(user, daily) { buildQuests(user, daily) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Задания",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandText
                )
                Text(
                    text = "Дневные цели в духе Duolingo. Возвращайся каждый день, чтобы не сбросить серию.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        items.forEach { quest ->
            item { QuestCard(quest) }
        }
    }
}

private data class Quest(
    val title: String,
    val description: String,
    val current: Int,
    val target: Int,
    val xp: Int,
    val emoji: String
)

private fun buildQuests(user: UserProfile?, daily: XpBoostStatus?): List<Quest> {
    val lessonsToday = daily?.lessonsToday ?: 0
    val xpToday = daily?.xpToday ?: 0
    val streakActive = isStreakActiveFor(user?.lastActiveDate)
    return listOf(
        Quest(
            title = "Пройди 1 урок сегодня",
            description = "Главное условие, чтобы серия росла",
            current = lessonsToday.coerceAtMost(1),
            target = 1,
            xp = 10,
            emoji = "🎯"
        ),
        Quest(
            title = "Продли серию",
            description = "Просто пройди любой урок на 100% — огонёк загорится снова",
            current = if (streakActive && (user?.streakDays ?: 0) > 0) 1 else 0,
            target = 1,
            xp = 15,
            emoji = "🔥"
        ),
        Quest(
            title = "Заработай 30 XP сегодня",
            description = "Один-два хороших урока и задание выполнено",
            current = xpToday.coerceAtMost(30),
            target = 30,
            xp = 20,
            emoji = "✨"
        )
    )
}

private fun isStreakActiveFor(date: String?): Boolean {
    if (date.isNullOrBlank()) return false
    return runCatching {
        java.time.LocalDate.parse(date.take(10)) == java.time.LocalDate.now()
    }.getOrDefault(false)
}

@Composable
private fun QuestCard(quest: Quest) {
    val done = quest.current >= quest.target
    val progress = if (quest.target > 0) (quest.current.toFloat() / quest.target).coerceIn(0f, 1f) else 0f
    val bg = if (done) BrandGreenSoft else BrandSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandOutline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(if (done) BrandGreen else BrandWarmSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = quest.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandText
                )
                Text(
                    text = quest.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandMuted
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = BrandGreen,
                    trackColor = BrandGreenSoft
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${quest.current} / ${quest.target}",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandMuted
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.xp),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            colorFilter = ColorFilter.tint(Color(0xFFD4A000))
                        )
                        Text(
                            text = "+${quest.xp} XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFD4A000),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
