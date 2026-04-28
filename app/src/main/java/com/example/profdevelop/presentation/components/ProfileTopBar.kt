package com.example.profdevelop.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandWarm
import java.time.LocalDate

private val FlameActive = Color(0xFFF18A1B)
private val FlameInactive = Color(0xFF9DA89E)
private val XpAccent = Color(0xFFE9A93E)

@Composable
fun ProfileTopBar(
    profile: UserProfile?,
    modifier: Modifier = Modifier,
    xpAnimationKey: Int = 0,
    xpProgressOverride: Float? = null
) {
    val streak = profile?.streakDays ?: 0
    val totalXp = profile?.totalXp ?: 0
    val level = profile?.level ?: 1
    val streakActive = profile?.let { isStreakActive(it.lastActiveDate) } ?: false

    val xpInLevel = totalXp - (level - 1) * 100
    val targetProgress = (xpInLevel.coerceAtLeast(0).toFloat() / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = xpProgressOverride ?: targetProgress,
        label = "xp-progress-$xpAnimationKey"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AvatarBadge(initials = initialsOf(profile?.fullName))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = profile?.fullName?.takeIf { it.isNotBlank() } ?: "Гость",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = "Уровень $level",
                style = MaterialTheme.typography.bodyMedium,
                color = BrandMuted,
                maxLines = 1
            )
        }

        StatChip(
            icon = {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (streakActive) FlameActive else FlameInactive,
                    modifier = Modifier.size(18.dp)
                )
            },
            label = streak.toString(),
            container = if (streakActive) BrandSurface else BrandOutline.copy(alpha = 0.45f)
        )

        XpChip(progress = animatedProgress, xp = xpInLevel.coerceAtLeast(0))
    }
}

@Composable
private fun AvatarBadge(initials: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(BrandGreenSoft),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = BrandGreen
        )
    }
}

@Composable
private fun StatChip(
    icon: @Composable () -> Unit,
    label: String,
    container: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon()
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun XpChip(progress: Float, xp: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BrandSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(XpAccent)
            )
            Text(
                text = "$xp XP",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .height(4.dp)
                .width(56.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BrandOutline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(56.dp * progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(XpAccent)
            )
        }
    }
}

private fun isStreakActive(lastActiveDate: String?): Boolean {
    if (lastActiveDate.isNullOrBlank()) return false
    return runCatching { LocalDate.parse(lastActiveDate.take(10)) == LocalDate.now() }
        .getOrDefault(false)
}

private fun initialsOf(fullName: String?): String {
    if (fullName.isNullOrBlank()) return "?"
    return fullName.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
}

