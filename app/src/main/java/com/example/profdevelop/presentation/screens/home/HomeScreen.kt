package com.example.profdevelop.presentation.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.profdevelop.domain.model.Course
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.presentation.components.ProfileTopBar
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandWarm

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenCourse: (Int, String) -> Unit,
    onOpenLesson: (Int, String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        ProfileTopBar(profile = state.profile)

        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGreen)
            }

            state.error != null -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> HomeContent(
                state = state,
                onOpenCourse = onOpenCourse,
                onOpenLesson = onOpenLesson
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenCourse: (Int, String) -> Unit,
    onOpenLesson: (Int, String) -> Unit
) {
    val cards = buildList {
        state.activeCourse?.let { add(CourseCardEntry(course = it, kind = CardKind.Continue)) }
        state.mandatoryCourses.forEach { add(CourseCardEntry(course = it, kind = CardKind.Mandatory)) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 0.dp,
            top = 8.dp,
            end = 0.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Ваш путь обучения",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Проходите уроки по порядку и открывайте следующий узел маршрута.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (cards.isNotEmpty()) {
            item {
                CoursePager(cards = cards, onOpenCourse = onOpenCourse)
            }
        }

        if (state.activeCourse != null) {
            item {
                Text(
                    text = "Маршрут курса",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            itemsIndexed(state.activeCourseLessons) { index, lesson ->
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    LessonPathNode(
                        lesson = lesson,
                        index = index,
                        isLast = index == state.activeCourseLessons.lastIndex,
                        onClick = {
                            if (lesson.isUnlocked) onOpenLesson(lesson.id, lesson.title)
                        }
                    )
                }
            }
        }
    }
}

private enum class CardKind { Continue, Mandatory }

private data class CourseCardEntry(
    val course: Course,
    val kind: CardKind
)

@Composable
private fun CoursePager(
    cards: List<CourseCardEntry>,
    onOpenCourse: (Int, String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { cards.size })

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp
        ) { page ->
            val entry = cards[page]
            CourseCard(entry = entry, onClick = { onOpenCourse(entry.course.id, entry.course.title) })
        }
        if (cards.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                cards.forEachIndexed { index, _ ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (active) 9.dp else 7.dp)
                            .background(
                                color = if (active) BrandGreen else BrandOutline,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseCard(entry: CourseCardEntry, onClick: () -> Unit) {
    val (badgeText, badgeColor, accent) = when (entry.kind) {
        CardKind.Continue -> Triple("Продолжить", BrandGreen, BrandGreenSoft)
        CardKind.Mandatory -> Triple("Назначенный курс", BrandWarm, Color(0xFFFFF1D6))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(accent, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelLarge,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (entry.course.isMandatory && entry.kind == CardKind.Continue) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFF1D6), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Обязательный",
                            style = MaterialTheme.typography.labelLarge,
                            color = BrandWarm,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = entry.course.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            entry.course.description?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = BrandMuted, maxLines = 3)
            }

            val pct = entry.course.progressPercent.coerceIn(0, 100)
            LinearProgressIndicator(
                progress = { pct / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(top = 4.dp),
                color = BrandGreen,
                trackColor = BrandOutline
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${entry.course.completedLessons} / ${entry.course.totalLessons} уроков",
                    color = BrandMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$pct%",
                    color = BrandGreen,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            entry.course.deadline?.let { deadline ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = if (entry.kind == CardKind.Mandatory) BrandWarm else BrandMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Дедлайн: ${formatDeadline(deadline)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (entry.kind == CardKind.Mandatory) BrandWarm else BrandMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonPathNode(
    lesson: Lesson,
    index: Int,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val horizontalOffset = when (index % 4) {
        0 -> 0.dp
        1 -> 54.dp
        2 -> 22.dp
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
        lesson.isCompleted -> "Пройден"
        lesson.isUnlocked -> "Старт"
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = horizontalOffset),
        horizontalAlignment = Alignment.Start
    ) {
        badgeText?.let {
            Text(
                text = it,
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 8.dp)
                    .background(BrandSurface, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = if (lesson.isCompleted) BrandGreen else BrandWarm,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .size(84.dp)
                .background(haloColor, CircleShape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(nodeColor, CircleShape)
                    .clickable(enabled = lesson.isUnlocked, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                when {
                    lesson.isCompleted -> Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = BrandSurface,
                        modifier = Modifier.size(32.dp)
                    )
                    lesson.isUnlocked -> Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = BrandSurface,
                        modifier = Modifier.size(32.dp)
                    )
                    else -> Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Урок закрыт",
                        tint = BrandMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        if (!isLast) {
            Spacer(
                modifier = Modifier
                    .padding(start = 39.dp, top = 6.dp, bottom = 0.dp)
                    .height(34.dp)
                    .size(width = 6.dp, height = 34.dp)
                    .background(BrandOutline, RoundedCornerShape(99.dp))
            )
        }
    }
}

private fun formatDeadline(value: String): String {
    val raw = value.take(10)
    val parts = raw.split("-")
    if (parts.size != 3) return raw
    val (y, m, d) = parts
    return "$d.$m.$y"
}
