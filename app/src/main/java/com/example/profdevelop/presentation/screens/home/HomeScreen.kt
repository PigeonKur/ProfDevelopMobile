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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.ColorFilter
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.profdevelop.R
import java.time.LocalDate
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.presentation.components.LessonPathNode
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
private val StreakGray     = Color(0xFF9DA89E)
private val StreakGrayBg   = Color(0xFFEFEFEF)
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
    onOpenLesson: (Int, String) -> Unit,
    onOpenProfile: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(refreshToken) {
        viewModel.load()
    }

    LaunchedEffect(state.boostMessage) {
        state.boostMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeBoostMessage()
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        TopBar(user = state.user, onProfileClick = onOpenProfile)
                    }

                    item {
                        BoostBanner(
                            secondsLeft = state.boostSecondsLeft,
                            onActivate = { viewModel.activateBoost() }
                        )
                    }

                    val nextLesson = state.nextLesson
                    val pages = buildList {
                        nextLesson?.let { add(HomePage.Continue(it)) }
                        state.chapters
                            .filter { it.course.isMandatory }
                            .sortedBy { it.course.deadline ?: "9999-12-31" }
                            .forEach { add(HomePage.Mandatory(it)) }
                    }

                    if (pages.isNotEmpty()) {
                        item {
                            HomePager(
                                pages = pages,
                                onOpenLesson = onOpenLesson,
                                onOpenCourse = onOpenCourse
                            )
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
private fun TopBar(user: UserProfile?, onProfileClick: () -> Unit) {
    val streakActive = isStreakActive(user?.lastActiveDate)
    val streakColor = if (streakActive) StreakOrange else StreakGray
    val streakBg = if (streakActive) StreakOrangeBg else StreakGrayBg

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(AvatarRingColor)
                .clickable(onClick = onProfileClick),
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

        TopBarStatChip(
            iconRes = R.drawable.burn,
            value = "${user?.streakDays ?: 0}",
            valueColor = streakColor,
            bgColor = streakBg,
            iconTint = if (streakActive) null else StreakGray
        )

        TopBarStatChip(
            iconRes = R.drawable.xp,
            value = "${user?.totalXp ?: 0}",
            valueColor = XpGold,
            bgColor = XpGoldBg
        )

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

private fun isStreakActive(lastActiveDate: String?): Boolean {
    if (lastActiveDate.isNullOrBlank()) return false
    return runCatching { LocalDate.parse(lastActiveDate.take(10)) == LocalDate.now() }
        .getOrDefault(false)
}


@Composable
private fun TopBarStatChip(
    @DrawableRes iconRes: Int,
    value: String,
    valueColor: Color,
    bgColor: Color,
    iconTint: Color? = null
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
                modifier = Modifier.size(20.dp),
                colorFilter = iconTint?.let { ColorFilter.tint(it) }
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
            LessonPathNode(
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

private sealed interface HomePage {
    data class Continue(val next: HomeNextLesson) : HomePage
    data class Mandatory(val chapter: HomeChapter) : HomePage
}

@Composable
private fun HomePager(
    pages: List<HomePage>,
    onOpenLesson: (Int, String) -> Unit,
    onOpenCourse: (Int, String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) { page ->
            when (val item = pages[page]) {
                is HomePage.Continue -> ContinueCard(
                    next = item.next,
                    onClick = { onOpenLesson(item.next.lesson.id, item.next.lesson.title) }
                )
                is HomePage.Mandatory -> MandatoryCourseCard(
                    chapter = item.chapter,
                    onClick = { onOpenCourse(item.chapter.course.id, item.chapter.course.title) }
                )
            }
        }

        if (pages.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (active) 8.dp else 6.dp)
                            .background(
                                color = if (active) BrandGreen else BrandRouteDivider,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueCard(next: HomeNextLesson, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

@Composable
private fun MandatoryCourseCard(chapter: HomeChapter, onClick: () -> Unit) {
    val deadlineText = chapter.course.deadline?.let { formatDeadline(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmSoft)
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
                    .background(BrandWarm),
                contentAlignment = Alignment.Center
            ) {
                Text("!", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Назначенный курс",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrandWarm,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = chapter.course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandText
                )
                val subtitle = buildString {
                    deadlineText?.let { append("До $it") }
                    if (chapter.course.totalLessons > 0) {
                        if (isNotEmpty()) append(" • ")
                        append("${chapter.completedCount}/${chapter.course.totalLessons} уроков")
                    }
                }
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandMuted
                    )
                }
            }
        }
    }
}

private fun formatDeadline(raw: String): String {
    return runCatching {
        val date = LocalDate.parse(raw.take(10))
        "%02d.%02d.%d".format(date.dayOfMonth, date.monthValue, date.year)
    }.getOrDefault(raw)
}
@Composable
private fun BoostBanner(
    secondsLeft: Int,
    onActivate: () -> Unit
) {
    val active = secondsLeft > 0
    val containerColor = if (active) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
    val borderColor = if (active) Color(0xFFFFB74D) else BrandGreen
    val accent = if (active) Color(0xFFE65100) else BrandGreen
    val title = if (active) "🚀 Двойной XP" else "🚀 Двойной XP"
    val subtitle = if (active) "Зарабатывай вдвое больше — каждое действие даёт 2x XP."
                   else "Активируй на 30 минут и получи 2x XP за каждый урок."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = !active, onClick = onActivate),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = accent
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = BrandMuted
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (active) formatBoostTime(secondsLeft) else "Включить",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun formatBoostTime(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}
