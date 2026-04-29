package com.example.profdevelop.presentation.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.profdevelop.R
import com.example.profdevelop.domain.model.LeaderboardEntry
import com.example.profdevelop.presentation.theme.BrandDanger
import com.example.profdevelop.presentation.theme.BrandDangerSoft
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandText

@Composable
internal fun LeaderboardSection(
    currentUserId: Int,
    entries: List<LeaderboardEntry>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    selectedTier: String? = null,
    onTierSelected: (String?) -> Unit = {},
    currentUserTier: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Лига",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = BrandText,
                modifier = Modifier.weight(1f)
            )
            currentUserTier?.let {
                Text(
                    text = "Твоя лига: ${tierTitle(it)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = tierColor(it),
                    fontWeight = FontWeight.Bold
                )
            } ?: Text(
                text = "Кто впереди на этой неделе",
                style = MaterialTheme.typography.labelSmall,
                color = BrandMuted
            )
        }

        TierTabs(
            selected = selectedTier,
            onSelect = onTierSelected
        )

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
    entry: LeaderboardEntry,
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
                        overflow = TextOverflow.Ellipsis,
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
                    entry.tier?.takeIf { it.isNotBlank() }?.let { tier ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(tierColor(tier))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tierTitle(tier),
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
                        overflow = TextOverflow.Ellipsis
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

@Composable
private fun TierTabs(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    val tabs = listOf<Pair<String?, String>>(
        null to "Все",
        "bronze" to "Бронза",
        "silver" to "Серебро",
        "gold" to "Золото",
        "diamond" to "Алмаз",
        "legendary" to "Легенда"
    )
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (key, title) ->
            val isSelected = selected.equals(key, ignoreCase = true) ||
                (selected == null && key == null)
            val tierC = key?.let { tierColor(it) } ?: BrandGreen
            val bg = if (isSelected) tierC else BrandSurface
            val fg = if (isSelected) Color.White else BrandText
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable { onSelect(key) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    color = fg,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

internal fun tierTitle(tier: String): String = when (tier.lowercase()) {
    "bronze" -> "Бронза"
    "silver" -> "Серебро"
    "gold" -> "Золото"
    "diamond" -> "Алмаз"
    "legendary" -> "Легенда"
    else -> tier
}

internal fun tierColor(tier: String): Color = when (tier.lowercase()) {
    "bronze" -> Color(0xFFCD7F32)
    "silver" -> Color(0xFF9DA89E)
    "gold" -> Color(0xFFD4A000)
    "diamond" -> Color(0xFF4FC3F7)
    "legendary" -> Color(0xFFAB47BC)
    else -> BrandGreen
}
