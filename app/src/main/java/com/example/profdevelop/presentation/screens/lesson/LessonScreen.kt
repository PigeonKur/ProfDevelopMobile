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
    // Перемешиваем правую колонку один раз на вопрос, чтобы правильные ответы
    // не оказывались параллельны левой колонке.
    val shuffledRight = remember(question.id) {
        val pairs = question.matchingPairs
        if (pairs.size < 2) {
            pairs
        } else {
            generateSequence { pairs.shuffled() }
                .first { shuffled ->
                    shuffled.withIndex().any { (i, p) -> pairs[i].id != p.id }
                }
        }
    }
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

                shuffledRight.forEach { pair ->
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
                maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    hyphens = androidx.compose.ui.text.style.Hyphens.Auto,
                    lineBreak = androidx.compose.ui.text.style.LineBreak.Paragraph
                ),
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

private val StreakOrange = Color(0xFFFF9600)
private val StreakGray = Color(0xFF9DA89E)
private val XpGold = Color(0xFFD4A000)

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
