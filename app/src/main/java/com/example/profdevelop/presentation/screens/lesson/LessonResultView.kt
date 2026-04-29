package com.example.profdevelop.presentation.screens.lesson

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.profdevelop.R
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandText
import com.example.profdevelop.presentation.theme.BrandWarm
import com.example.profdevelop.presentation.theme.BrandWarmSoft
import com.example.profdevelop.presentation.util.Haptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val StreakOrange = Color(0xFFFF9600)
private val StreakGray = Color(0xFF9DA89E)
private val XpGold = Color(0xFFD4A000)

@Composable
internal fun LessonResultView(
    title: String,
    state: LessonUiState,
    onFinish: () -> Unit
) {
    val result = state.result ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val xpProgress = remember(result.xpEarned) { Animatable(0f) }
    val xpCounter by remember(result.xpEarned) {
        derivedStateOf { (xpProgress.value * result.xpEarned).toInt() }
    }
    val streakScale = remember(result.streakDays) { Animatable(1f) }

    val perfect = result.score == result.maxScore && result.maxScore > 0
    val accentColor = if (perfect) BrandGreen else BrandWarm
    val accentSoft = if (perfect) BrandGreenSoft else BrandWarmSoft
    val streakIconColor = if (result.streakActive) StreakOrange else StreakGray

    LaunchedEffect(result.xpEarned, result.streakIncreased) {
        delay(150)
        scope.launch {
            // Заполняем XP-шкалу с лёгкими тиками-вибрациями.
            val ticks = 6
            val durationPerTick = 80L
            repeat(ticks) { i ->
                xpProgress.animateTo(
                    targetValue = (i + 1f) / ticks,
                    animationSpec = tween(durationMillis = durationPerTick.toInt())
                )
                Haptics.xpTick(context)
            }
        }
        if (result.streakIncreased) {
            delay(450)
            Haptics.streakFlame(context)
            streakScale.animateTo(1.25f, tween(120))
            streakScale.animateTo(1f, tween(180))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(96.dp)
                .background(accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.approved),
                contentDescription = "Пройден",
                modifier = Modifier.size(48.dp),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (perfect) "Урок пройден!" else "Урок завершён",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = BrandText
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(accentSoft)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                color = accentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandOutline)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResultStatBlock(
                        title = "Точность",
                        value = if (result.maxScore > 0) {
                            "${(result.score * 100 / result.maxScore)}%"
                        } else "—",
                        valueColor = accentColor
                    )
                    ResultStatBlock(
                        title = "Серия",
                        value = "${result.streakDays}",
                        valueColor = streakIconColor,
                        iconRes = R.drawable.burn,
                        iconTint = streakIconColor,
                        scale = streakScale.value
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.xp),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Полученные XP",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrandMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "+$xpCounter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = XpGold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { xpProgress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = XpGold,
                        trackColor = accentSoft
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text(
                text = "К маршруту",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ResultStatBlock(
    title: String,
    value: String,
    valueColor: Color,
    iconRes: Int? = null,
    iconTint: Color? = null,
    scale: Float = 1f
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = BrandMuted,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    colorFilter = iconTint?.let { ColorFilter.tint(it) }
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
        }
    }
}
