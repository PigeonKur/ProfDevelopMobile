package com.example.profdevelop.presentation.screens.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                                        question.matchingPairs.forEach { left ->
                                            MatchingItem(
                                                left = left,
                                                options = question.matchingPairs,
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 32.dp,
            end = 20.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (result.isCompleted) BrandGreenSoft else BrandSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (result.isCompleted) "Урок пройден" else "Урок не пройден",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Результат: ${result.score} / ${result.maxScore}", color = BrandMuted)
                    Text("Получено XP: ${result.xpEarned}", color = BrandGreen, fontWeight = FontWeight.Bold)
                    Text("Уровень: ${result.newLevel} • Серия: ${result.streakDays}", color = BrandMuted)
                }
            }
        }

        items(result.questionReviews) { review ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (review.isCorrect) "Верно" else "Нужно повторить",
                        color = if (review.isCorrect) BrandGreen else BrandDanger,
                        fontWeight = FontWeight.Bold
                    )
                    review.explanation?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = BrandMuted)
                    }
                }
            }
        }

        item {
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("К маршруту")
            }
        }
    }
}
