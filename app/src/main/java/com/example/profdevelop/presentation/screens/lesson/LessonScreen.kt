package com.example.profdevelop.presentation.screens.lesson

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 28.dp,
                            end = 20.dp,
                            bottom = 150.dp
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

                            when {
                                question.type != "truefalse" || state.feedback != null -> {
                                    Button(
                                        onClick = {
                                            if (state.feedback != null) {
                                                viewModel.continueAfterFeedback()
                                            } else {
                                                viewModel.checkCurrentQuestion()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !state.isChecking && !state.isSubmitting
                                    ) {
                                        when {
                                            state.isChecking || state.isSubmitting -> {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }

                                            state.feedback != null -> Text("Продолжить")
                                            else -> Text("Проверить")
                                        }
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }

                    state.feedback?.let { feedback ->
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
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
                                verticalArrangement = Arrangement.spacedBy(6.dp)
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
private fun MatchingBoard(
    question: Question,
    selectedMatches: Map<Int, Int>,
    onSelect: (Int, Int) -> Unit
) {
    var activeLeftId by remember(question.id) { mutableIntStateOf(question.matchingPairs.firstOrNull()?.id ?: -1) }
    val expandedLeft = remember(question.id) { mutableStateMapOf<Int, Boolean>() }
    val expandedRight = remember(question.id) { mutableStateMapOf<Int, Boolean>() }
    val palette = listOf(
        Color(0xFFEAF2FF),
        Color(0xFFFFF4D9),
        Color(0xFFF4E7DA),
        Color(0xFFE8F8E6),
        Color(0xFFF6E7FF),
        Color(0xFFFFE8E0)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Выберите элемент слева, затем соответствие справа.",
            style = MaterialTheme.typography.bodySmall,
            color = BrandMuted
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Левая колонка",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrandMuted
                )

                question.matchingPairs.forEachIndexed { index, pair ->
                    val pairColor = selectedMatches[pair.id]?.let { palette[index % palette.size] }
                    val selectedRightId = selectedMatches[pair.id]
                    ExpandableMatchingCard(
                        text = pair.leftText,
                        isExpanded = expandedLeft[pair.id] == true,
                        isActive = activeLeftId == pair.id && selectedRightId == null,
                        backgroundColor = pairColor,
                        assignmentText = selectedRightId?.let { rightId ->
                            question.matchingPairs.firstOrNull { it.id == rightId }?.rightText
                        },
                        onToggleExpand = {
                            expandedLeft[pair.id] = !(expandedLeft[pair.id] ?: false)
                        },
                        onClick = { activeLeftId = pair.id }
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Правая колонка",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrandMuted
                )

                question.matchingPairs.forEach { pair ->
                    val usedByLeftId = selectedMatches.entries.firstOrNull { it.value == pair.id }?.key
                    val leftIndex = question.matchingPairs.indexOfFirst { it.id == usedByLeftId }
                    val pairColor = if (leftIndex >= 0) palette[leftIndex % palette.size] else null

                    ExpandableMatchingCard(
                        text = pair.rightText,
                        isExpanded = expandedRight[pair.id] == true,
                        isActive = false,
                        backgroundColor = pairColor,
                        assignmentText = usedByLeftId?.let { leftId ->
                            question.matchingPairs.firstOrNull { it.id == leftId }?.leftText
                        },
                        onToggleExpand = {
                            expandedRight[pair.id] = !(expandedRight[pair.id] ?: false)
                        },
                        onClick = {
                            if (activeLeftId != -1) {
                                onSelect(activeLeftId, pair.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableMatchingCard(
    text: String,
    isExpanded: Boolean,
    isActive: Boolean,
    backgroundColor: Color?,
    assignmentText: String?,
    onToggleExpand: () -> Unit,
    onClick: () -> Unit
) {
    var hasOverflow by remember(text) { mutableStateOf(false) }
    val background = backgroundColor ?: if (isActive) BrandWarmSoft else BrandSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                onTextLayout = { hasOverflow = it.hasVisualOverflow }
            )

            assignmentText?.let {
                Text(
                    text = "Связано: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (hasOverflow || isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = if (isExpanded) "Свернуть" else "Подробнее",
                        modifier = Modifier.clickable(onClick = onToggleExpand),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isActive) BrandWarm else BrandMuted,
                        fontWeight = FontWeight.Bold
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
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BrandGreenSoft)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(BrandGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.approved),
                            contentDescription = "Пройден",
                            modifier = Modifier.size(36.dp),

                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    Text(
                        text = "Урок пройден",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = title,
                        color = BrandMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ResultStatChip("Результат", "${result.score} / ${result.maxScore}")
                        ResultStatChip("XP", "${result.xpEarned}")
                        ResultStatChip("Серия", "${result.streakDays}")
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

@Composable
private fun ResultStatChip(
    title: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .width(96.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = BrandMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
