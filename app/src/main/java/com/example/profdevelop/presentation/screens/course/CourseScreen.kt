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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandRouteDivider
import com.example.profdevelop.presentation.theme.BrandRouteLine
import com.example.profdevelop.presentation.theme.BrandRouteLocked
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandWarm
import com.example.profdevelop.presentation.theme.BrandWarmSoft

@Composable
fun CourseScreen(
    title: String,
    viewModel: CourseViewModel,
    refreshToken: Int,
    onOpenLesson: (Int, String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(refreshToken) {
        if (refreshToken > 0) {
            viewModel.load()
        }
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
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = 24.dp,
                        end = 20.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(BrandRouteDivider, RoundedCornerShape(999.dp))
                        )
                    }

                    item {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Уроки главы идут по маршруту сверху вниз.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandMuted
                        )
                    }

                    itemsIndexed(state.lessons) { index, lesson ->
                        CourseLessonNode(
                            lesson = lesson,
                            index = index,
                            isLast = index == state.lessons.lastIndex,
                            onClick = {
                                if (lesson.isUnlocked) {
                                    onOpenLesson(lesson.id, lesson.title)
                                }
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
    val horizontalOffset = when (index % 5) {
        0 -> (-26).dp
        1 -> 38.dp
        2 -> 0.dp
        3 -> (-38).dp
        else -> 26.dp
    }

    val nodeColor = when {
        lesson.isCompleted -> BrandGreen
        lesson.isUnlocked -> BrandWarm
        else -> BrandRouteLocked
    }

    val haloColor = when {
        lesson.isCompleted -> BrandGreenSoft
        lesson.isUnlocked -> BrandWarmSoft
        else -> BrandSurface
    }

    val nodeText = when {
        lesson.isCompleted -> "★"
        lesson.isUnlocked -> "${lesson.orderIndex}"
        else -> null
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.offset(x = horizontalOffset),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = lesson.title,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .background(BrandSurface, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = BrandMuted,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

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
                        .clickable(enabled = lesson.isUnlocked, onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (nodeText != null) {
                        Text(
                            text = nodeText,
                            color = if (lesson.isUnlocked || lesson.isCompleted) BrandSurface else BrandMuted,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Заблокировано",
                            tint = BrandSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Text(
                text = "${lesson.xpReward} XP • ${lesson.estimatedMinutes} мин",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = BrandMuted
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .height(34.dp)
                        .size(width = 6.dp, height = 34.dp)
                        .background(BrandRouteLine, RoundedCornerShape(999.dp))
                )
            }
        }
    }
}
