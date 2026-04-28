package com.example.profdevelop.presentation.screens.lesson

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.profdevelop.domain.model.AnswerOption
import com.example.profdevelop.domain.model.MatchingPair
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandDanger
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandWarm

@Composable
fun LessonScreen(
    title: String,
    viewModel: LessonViewModel,
    onFinished: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val question = state.currentQuestion

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

            state.showResult && state.result != null -> {
                LessonResultView(
                    title = title,
                    state = state,
                    onFinish = onFinished
                )
            }

            question != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = 32.dp,
                        end = 20.dp,
                        bottom = 28.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = { (state.currentQuestionIndex + 1f) / state.questions.size.coerceAtLeast(1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            color = BrandGreen
                        )
                        Text(
                            text = "Вопрос ${state.currentQuestionIndex + 1} из ${state.questions.size}",
                            modifier = Modifier.padding(top = 8.dp),
                            color = BrandMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BrandSurface),
                            shape = RoundedCornerShape(28.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = question.text,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                question.hint?.takeIf { it.isNotBlank() }?.let {
                                    Text(it, color = BrandMuted, style = MaterialTheme.typography.bodyMedium)
                                }

                                when (question.type) {
                                    "choice", "true_false" -> {
                                        question.answers.forEach { answer ->
                                            AnswerItem(
                                                answer = answer,
                                                selected = state.selectedAnswers[question.id]?.contains(answer.id) == true
                                            ) {
                                                viewModel.selectAnswer(question.id, answer.id)
                                            }
                                        }
                                    }

                                    "matching" -> {
                                        val shuffled = state.shuffledRightOptions[question.id]
                                            ?: question.matchingPairs
                                        question.matchingPairs.forEach { left ->
                                            MatchingItem(
                                                left = left,
                                                options = shuffled,
                                                selectedRightId = state.selectedMatches[question.id]?.get(left.id),
                                                onSelect = { rightId ->
                                                    viewModel.selectMatching(question.id, left.id, rightId)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        state.error?.let {
                            Text(it, color = BrandDanger)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { viewModel.previousQuestion() },
                                enabled = state.canGoBack,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Назад")
                            }
                            if (state.isLastQuestion) {
                                Button(
                                    onClick = { viewModel.submit() },
                                    enabled = !state.isSubmitting,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (state.isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(2.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text("Завершить")
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.nextQuestion() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Далее")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerItem(
    answer: AnswerOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) BrandGreenSoft else BrandSurface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = answer.text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun MatchingItem(
    left: MatchingPair,
    options: List<MatchingPair>,
    selectedRightId: Int?,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(left.leftText, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                Card(
                    modifier = Modifier.clickable { onSelect(option.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedRightId == option.id) BrandWarm else BrandSurface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = option.rightText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        color = if (selectedRightId == option.id) BrandSurface else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonResultView(
    title: String,
    state: LessonUiState,
    onFinish: () -> Unit
) {
    val result = state.result ?: return
    val context = androidx.compose.ui.platform.LocalContext.current

    // Анимации XP и серии — заполняются после короткой паузы.
    val streakAnim = remember { Animatable(result.previousStreak.toFloat()) }
    val xpInLevel = result.totalXp - (result.newLevel - 1) * 100
    val xpStart = (xpInLevel - result.xpEarned).coerceAtLeast(0)
    val xpAnim = remember { Animatable(xpStart.toFloat()) }

    LaunchedEffect(result) {
        if (result.streakIncreased) {
            kotlinx.coroutines.delay(250)
            com.example.profdevelop.presentation.util.Haptics.streakFlame(context)
            streakAnim.animateTo(
                targetValue = result.streakDays.toFloat(),
                animationSpec = tween(durationMillis = 600)
            )
        }
        if (result.xpEarned > 0) {
            kotlinx.coroutines.delay(150)
            com.example.profdevelop.presentation.util.Haptics.xpTick(context)
            xpAnim.animateTo(
                targetValue = xpInLevel.coerceAtLeast(0).toFloat(),
                animationSpec = tween(durationMillis = 700)
            )
        }
    }

    val passColor = if (result.isCompleted) BrandGreen else BrandWarm
    val passBg = if (result.isCompleted) BrandGreenSoft else BrandSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        // Шапка с названием — не растёт, сидит сверху.
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = BrandMuted,
            modifier = Modifier.padding(top = 16.dp)
        )

        // Главный блок с результатом — занимает оставшееся пространство и
        // центрируется по вертикали, чтобы не висеть под статусбаром.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ResultMedal(passed = result.isCompleted)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (result.isCompleted) "Урок пройден" else "Почти получилось",
                style = MaterialTheme.typography.headlineLarge,
                color = passColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (result.isCompleted) "Молодец! Идёшь в правильном направлении." else "Попробуй ещё раз — ты близко.",
                style = MaterialTheme.typography.bodyLarge,
                color = BrandMuted
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Карточки статистики на одной строке: точность / xp / серия.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Точность",
                    value = "${if (result.maxScore > 0) result.score * 100 / result.maxScore else 0}%",
                    accent = passColor,
                    container = passBg
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "XP",
                    value = "+${result.xpEarned}",
                    accent = Color(0xFFE9A93E),
                    container = Color(0xFFFFF6E0),
                    progress = xpAnim.value / 100f
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Серия",
                    value = "${streakAnim.value.toInt()}",
                    accent = if (result.streakActive) Color(0xFFF18A1B) else BrandMuted,
                    container = if (result.streakActive) Color(0xFFFFEEDD) else BrandSurface,
                    iconResource = "🔥".takeIf { result.streakActive }
                )
            }

            if (result.newAchievements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Новые достижения",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        result.newAchievements.forEach { ach ->
                            Text(
                                text = "${ach.icon ?: "🏅"}  ${ach.title}",
                                color = BrandGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Краткий разбор ошибок (если есть).
            val incorrect = result.questionReviews.filter { !it.isCorrect }
            if (incorrect.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Что повторить",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(incorrect) { review ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandSurface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Нужно повторить",
                                    color = BrandDanger,
                                    fontWeight = FontWeight.Bold
                                )
                                review.explanation?.takeIf { it.isNotBlank() }?.let {
                                    Text(it, color = BrandMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 12.dp)
        ) {
            Text("К маршруту")
        }
    }
}

@Composable
private fun ResultMedal(passed: Boolean) {
    val bg = if (passed) BrandGreen else BrandWarm
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(bg, shape = RoundedCornerShape(60.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (passed) "★" else "↻",
            color = BrandSurface,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    container: androidx.compose.ui.graphics.Color,
    progress: Float? = null,
    iconResource: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = BrandMuted
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                iconResource?.let {
                    Text(it, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.padding(end = 4.dp))
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    color = accent,
                    trackColor = BrandSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
