package com.example.profdevelop.presentation.screens.lesson

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.util.Haptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import com.example.profdevelop.presentation.theme.BrandText
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.profdevelop.R
import com.example.profdevelop.domain.model.AnswerOption
import com.example.profdevelop.domain.model.MatchingPair
import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandDanger
import com.example.profdevelop.presentation.theme.BrandDangerSoft
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandWarm
import com.example.profdevelop.presentation.theme.BrandWarmSoft

@Composable
fun LessonScreen(
    title: String,
    viewModel: LessonViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val question = state.currentQuestion

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        val feedbackMaxHeight = maxHeight * 0.4f

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 28.dp,
                            end = 20.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "← Назад",
                                    modifier = Modifier
                                        .background(BrandSurface, RoundedCornerShape(16.dp))
                                        .clickable { onBack() }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Урок",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = BrandMuted
                                )
                            }
                        }

                        item {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            LinearProgressIndicator(
                                progress = { state.completedQuestionIds.size.toFloat() / state.questions.size.coerceAtLeast(1) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                color = BrandGreen
                            )
                            Text(
                                text = "Освоено ${state.progressLabel}",
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
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                        "choice" -> {
                                            Text(
                                                text = "Можно выбрать несколько вариантов",
                                                color = BrandMuted,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            question.answers.forEach { answer ->
                                                AnswerItem(
                                                    answer = answer,
                                                    selected = state.selectedAnswers[question.id]?.contains(answer.id) == true
                                                ) {
                                                    viewModel.selectAnswer(question.id, answer.id)
                                                }
                                            }
                                        }

                                        "truefalse" -> {
                                            Text(
                                                text = "Выберите один из двух вариантов ниже",
                                                color = BrandMuted,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            val trueAnswer = question.answers.firstOrNull {
                                                it.text.trim().equals("Правда", ignoreCase = true)
                                            } ?: question.answers.maxByOrNull { it.orderIndex }
                                            val falseAnswer = question.answers.firstOrNull {
                                                it.text.trim().equals("Ложь", ignoreCase = true) ||
                                                    it.text.trim().equals("Неправда", ignoreCase = true)
                                            } ?: question.answers
                                                .filter { it.id != trueAnswer?.id }
                                                .minByOrNull { it.orderIndex }

                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                falseAnswer?.let { answer ->
                                                    Button(
                                                        onClick = {
                                                            viewModel.selectAnswer(question.id, answer.id)
                                                            viewModel.checkCurrentQuestion()
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        enabled = !state.isChecking && !state.isSubmitting && state.feedback == null
                                                    ) {
                                                        Text("Ложь")
                                                    }
                                                }

                                                trueAnswer?.let { answer ->
                                                    Button(
                                                        onClick = {
                                                            viewModel.selectAnswer(question.id, answer.id)
                                                            viewModel.checkCurrentQuestion()
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        enabled = !state.isChecking && !state.isSubmitting && state.feedback == null
                                                    ) {
                                                        Text("Правда")
                                                    }
                                                }
                                            }
                                        }

                                        "matching" -> {
                                            MatchingBoard(
                                                question = question,
                                                selectedMatches = state.selectedMatches[question.id].orEmpty(),
                                                onSelect = { leftId, rightId ->
                                                    viewModel.selectMatching(question.id, leftId, rightId)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            state.error?.let {
                                Text(it, color = BrandDanger)
                            }
                        }
                    }

                    // Sticky-плашка снизу: либо фидбэк после ответа, либо
                    // обычная кнопка «Проверить».
                    val feedback = state.feedback
                    if (feedback != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 96.dp, max = feedbackMaxHeight)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (feedback.isCorrect) BrandGreenSoft else BrandDangerSoft
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (feedback.isCorrect) "Верно" else "Неверно",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (feedback.isCorrect) BrandGreen else BrandDanger
                                )
                                Text(
                                    text = feedback.message,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = { viewModel.continueAfterFeedback() },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isChecking && !state.isSubmitting,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (feedback.isCorrect) BrandGreen else BrandDanger
                                    )
                                ) {
                                    if (state.isChecking || state.isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text("Дальше", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else if (question.type != "truefalse") {
                        Button(
                            onClick = { viewModel.checkCurrentQuestion() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            enabled = !state.isChecking && !state.isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                        ) {
                            if (state.isChecking || state.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Проверить", color = Color.White, fontWeight = FontWeight.Bold)
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

