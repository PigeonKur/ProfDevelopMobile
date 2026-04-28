package com.example.profdevelop.presentation.screens.course

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandWarm

@Composable
fun CourseScreen(
    title: String,
    viewModel: CourseViewModel,
    onOpenLesson: (Int, String) -> Unit
) {
    val state by viewModel.state.collectAsState()

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
                val total = state.lessons.size.coerceAtLeast(1)
                val completed = state.lessons.count { it.isCompleted }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = 24.dp,
                        end = 20.dp,
                        bottom = 28.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandSurface)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$completed из $total уроков завершено",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BrandGreen
                                )
                                Text(
                                    text = "Открытые узлы доступны для прохождения сразу.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BrandMuted
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Последовательность уроков",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    itemsIndexed(state.lessons) { index, lesson ->
                        CourseLessonNode(
                            lesson = lesson,
                            index = index,
                            isLast = index == state.lessons.lastIndex,
                            onClick = {
                                if (lesson.isUnlocked) onOpenLesson(lesson.id, lesson.title)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseLessonNode(
    lesson: Lesson,
    index: Int,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val horizontalOffset = when (index % 4) {
        0 -> 0.dp
        1 -> 52.dp
        2 -> 20.dp
        else -> 72.dp
    }
    val nodeColor = when {
        lesson.isCompleted -> BrandGreen
        lesson.isUnlocked -> BrandWarm
        else -> BrandOutline
    }
    val haloColor = when {
        lesson.isCompleted -> BrandGreenSoft
        lesson.isUnlocked -> BrandSurface
        else -> BrandSurface
    }
    val badgeText = when {
        lesson.isCompleted -> "Готово"
        lesson.isUnlocked -> "Доступно"
        else -> "Закрыто"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = horizontalOffset),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = badgeText,
            modifier = Modifier
                .padding(start = 8.dp, bottom = 8.dp)
                .background(BrandSurface, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (lesson.isUnlocked || lesson.isCompleted) BrandGreen else BrandMuted,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .size(88.dp)
                .background(haloColor, CircleShape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(nodeColor, CircleShape)
                    .clickable(enabled = lesson.isUnlocked, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                when {
                    lesson.isCompleted -> Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = BrandSurface,
                        modifier = Modifier.size(36.dp)
                    )
                    lesson.isUnlocked -> Text(
                        text = "${lesson.orderIndex}",
                        color = BrandSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    else -> Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Урок закрыт",
                        tint = BrandMuted,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Text(
            text = "${lesson.xpReward} XP • ${lesson.estimatedMinutes} мин",
            modifier = Modifier.padding(start = 8.dp, top = 8.dp),
            color = BrandMuted,
            style = MaterialTheme.typography.bodyMedium
        )

        if (!isLast) {
            Box(
                modifier = Modifier
                    .padding(start = 41.dp, top = 8.dp)
                    .height(30.dp)
                    .size(width = 6.dp, height = 30.dp)
                    .background(BrandOutline, RoundedCornerShape(99.dp))
            )
        }
    }
}
