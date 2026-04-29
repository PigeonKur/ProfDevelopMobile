package com.example.profdevelop.presentation.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandDanger
import com.example.profdevelop.presentation.theme.BrandDangerSoft
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandText
import com.example.profdevelop.presentation.theme.BrandWarm
import com.example.profdevelop.presentation.theme.BrandWarmSoft

@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel,
    onClose: () -> Unit = {}
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
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            state.questions.isEmpty() -> EmptyState()
            state.finished -> SummaryView(
                correct = state.correctCount,
                total = state.total,
                onAgain = viewModel::load,
                onClose = onClose
            )
            else -> QuestionView(state, viewModel)
        }
    }
}

@Composable
private fun QuestionView(state: PracticeUiState, viewModel: PracticeViewModel) {
    val question = state.currentQuestion ?: return
    val feedback = state.feedback
    val progress = if (state.total > 0) (state.currentIndex + 1).toFloat() / state.total else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Практика",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = BrandText
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Вопрос ${state.currentIndex + 1} из ${state.total}",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandMuted
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = BrandGreen,
            trackColor = BrandGreenSoft
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandOutline)
        ) {
            Text(
                text = question.text,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = BrandText
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(question.answers, key = { it.id }) { answer ->
                val selected = answer.id in state.selected
                val locked = feedback != null
                val bg = when {
                    selected -> BrandWarmSoft
                    else -> BrandSurface
                }
                val border = when {
                    selected -> BrandWarm
                    else -> BrandOutline
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !locked) { viewModel.selectAnswer(answer.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bg),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, border)
                ) {
                    Text(
                        text = answer.text,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = BrandText
                    )
                }
            }
        }

        if (state.error != null && feedback == null) {
            Text(
                text = state.error,
                color = BrandDanger,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (feedback != null) {
            FeedbackBar(feedback)
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = viewModel::next,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (feedback.isCorrect) BrandGreen else BrandDanger
                )
            ) {
                Text(
                    text = if (state.currentIndex == state.total - 1) "Завершить" else "Дальше",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = viewModel::check,
                enabled = !state.isChecking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Text(
                    text = if (state.isChecking) "Проверяем..." else "Проверить",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeedbackBar(feedback: PracticeFeedback) {
    val bg = if (feedback.isCorrect) BrandGreenSoft else BrandDangerSoft
    val color = if (feedback.isCorrect) BrandGreen else BrandDanger
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (feedback.isCorrect) "Верно!" else "Неверно",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            if (!feedback.explanation.isNullOrBlank()) {
                Text(
                    text = feedback.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryView(correct: Int, total: Int, onAgain: () -> Unit, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(BrandGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("★", color = Color.White, fontSize = MaterialTheme.typography.displaySmall.fontSize)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Тренировка завершена",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = BrandText
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Правильных ответов: $correct из $total",
                style = MaterialTheme.typography.bodyLarge,
                color = BrandMuted
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Text("Ещё раз", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandSurface)
            ) {
                Text("К главной", color = BrandText, fontWeight = FontWeight.Bold)
            }
        }

        // Крестик в правом-верхнем углу — быстрый выход на главную.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(BrandSurface)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text("✕", color = BrandText, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📚",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Пока нечего повторять",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BrandText
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Пройди хотя бы один урок — и здесь появятся вопросы для повторения.",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = BrandDanger
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
            Text("Повторить", color = Color.White)
        }
    }
}
