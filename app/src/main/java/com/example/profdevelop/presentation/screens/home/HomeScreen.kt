package com.example.profdevelop.presentation.screens.home

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import com.example.profdevelop.R
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandRouteDivider
import com.example.profdevelop.presentation.theme.BrandRouteLocked
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandText
import com.example.profdevelop.presentation.theme.BrandWarm
import com.example.profdevelop.presentation.theme.BrandWarmSoft

private val StreakOrange   = Color(0xFFFF9600)
private val StreakOrangeBg = Color(0xFFFFF3E0)
private val XpGold         = Color(0xFFD4A000)
private val XpGoldBg       = Color(0xFFFFF9E0)
private val AvatarRingColor = Color(0xFF58CC02)
private val AvatarBg       = Color(0xFFE8F5E9)
private val LevelBg        = Color(0xFFE3F2E6)
private val LevelColor     = Color(0xFF2E7D32)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    refreshToken: Int,
    onOpenCourse: (Int, String) -> Unit,
    onOpenLesson: (Int, String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(refreshToken) {
        if (refreshToken > 0) viewModel.load()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = BrandGreen
            )

            state.error != null -> Text(
                text = state.error.orEmpty(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                color = MaterialTheme.colorScheme.error
            )

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        TopBar(user = state.user)
                    }

                    state.nextLesson?.let { next ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                                    .clickable { onOpenLesson(next.lesson.id, next.lesson.title) },
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandGreenSoft)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(BrandGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("▶", color = Color.White, fontSize = 18.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Продолжить",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = BrandGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = next.lesson.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandText
                                        )
                                        Text(
                                            text = "${next.courseTitle} • ${next.lesson.xpReward} XP",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BrandMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(state.chapters) { chapter ->
                        CourseSection(
                            chapter = chapter,
                            onOpenLesson = onOpenLesson
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun TopBar(user: UserProfile?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(AvatarRingColor),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AvatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.fullName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontWeight = FontWeight.ExtraBold,
                    color = AvatarRingColor,
                    fontSize = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TopBarStatChip(
            iconRes = R.drawable.burn,
            value = "${user?.streakDays ?: 0}",
            valueColor = StreakOrange,
            bgColor = StreakOrangeBg
        )

        Spacer(modifier = Modifier.width(8.dp))

        TopBarStatChip(
            iconRes = R.drawable.xp,
            value = "${user?.totalXp ?: 0}",
            valueColor = XpGold,
            bgColor = XpGoldBg
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(LevelBg)
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Ур.",
                    fontSize = 12.sp,
                    color = LevelColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${user?.level ?: 1}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LevelColor
                )
            }
        }
    }
}


@Composable
private fun TopBarStatChip(
    @DrawableRes iconRes: Int,
    value: String,
    valueColor: Color,
    bgColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}


@Composable
private fun CourseSection(
    chapter: HomeChapter,
    onOpenLesson: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CourseDivider(
            title = chapter.course.title,
            progress = "${chapter.completedCount} из ${chapter.lessons.size} уроков"
        )

        chapter.lessons.forEachIndexed { index, lesson ->
            CourseLessonNode(
                lesson = lesson,
                index = index,
                isLast = index == chapter.lessons.lastIndex,
                onClick = {
                    if (lesson.isUnlocked) onOpenLesson(lesson.id, lesson.title)
                }
            )
        }
    }
}

@Composable
private fun CourseDivider(title: String, progress: String) {
    Column(
        modifier = Modifier.padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(BrandRouteDivider, RoundedCornerShape(999.dp))
        )
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = progress, style = MaterialTheme.typography.bodyMedium, color = BrandMuted)
    }
}

@Composable
private fun CourseLessonNode(
    lesson: Lesson,
    index: Int,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val horizontalOffset = when (index % 5) {
        0 -> (-26).dp
        1 -> 40.dp
        2 -> 0.dp
        3 -> (-40).dp
        else -> 26.dp
    }
    val nodeColor = when {
        lesson.isCompleted -> BrandGreen
        lesson.isUnlocked  -> BrandWarm
        else               -> BrandRouteLocked
    }
    val haloColor = when {
        lesson.isCompleted -> BrandGreenSoft
        lesson.isUnlocked  -> BrandWarmSoft
        else               -> BrandSurface
    }
    val nodeText = when {
        lesson.isCompleted -> R.drawable.approved
        lesson.isUnlocked  -> "${lesson.orderIndex}"
        else               -> "•"
    }
    val stepRotation = when (index % 5) {
        0 -> 28f; 1 -> -34f; 2 -> 32f; 3 -> -30f; else -> 26f
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.offset(x = horizontalOffset),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(haloColor, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(nodeColor, CircleShape)
                        .clickable(
                            enabled = lesson.isUnlocked || lesson.isCompleted,
                            onClick = onClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        lesson.isCompleted -> {
                            Image(
                                painter = painterResource(id = R.drawable.approved),
                                contentDescription = "Пройден",
                                modifier = Modifier.size(36.dp),

                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                        lesson.isUnlocked -> {
                            Text(
                                text = "${lesson.orderIndex}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> {
                            Text(
                                text = "•",
                                color = BrandMuted,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (!isLast) StepsConnector(rotation = stepRotation)
        }
    }
}

@Composable
private fun StepsConnector(rotation: Float) {
    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .rotate(rotation),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(BrandRouteDivider, CircleShape)
            )
        }
    }
}