package com.example.profdevelop.presentation.screens.lesson

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

typealias FeedbackState = LessonFeedback

@Composable
fun LessonScreen(
    state: LessonUiState,
    onSelectAnswer: (Int, Int) -> Unit,
    onSelectMatching: (Int, Int, Int) -> Unit,
    onCheck: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onExit: () -> Unit,
    onFinish: () -> Unit
) {
    if (state.isLoading) {
        CircularProgressIndicator(modifier = Modifier.testTag("lesson_loading"))
        return
    }
    if (state.showResult && state.result != null) {
        Column(modifier = Modifier.testTag("result_screen")) {
            Text("Результат")
            Button(onClick = onFinish) { Text("Готово") }
        }
        return
    }

    val question = state.currentQuestion
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Выход", modifier = Modifier.testTag("exit_button").clickable(onClick = onExit))
        LinearProgressIndicator(
            progress = { 0.5f },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lesson_progress_bar")
        )
        Text("${state.completedQuestionIds.size} из ${if (state.totalUniqueCount > 0) state.totalUniqueCount else state.questions.size}")
        if (question != null) {
            Text(question.text)
            question.hint?.let { Text(it) }
            question.answers.forEach { answer ->
                Text(answer.text, modifier = Modifier.clickable { onSelectAnswer(question.id, answer.id) })
            }
            question.matchingPairs.forEach { pair ->
                Row {
                    Text(pair.leftText, modifier = Modifier.clickable { onSelectMatching(question.id, pair.id, pair.id) })
                    Text(pair.rightText, modifier = Modifier.clickable { onSelectMatching(question.id, pair.id, pair.id) })
                }
            }
        }

        state.feedback?.let {
            Column(modifier = Modifier.testTag("feedback_panel")) {
                Text(it.message)
                Button(onClick = onContinue, modifier = Modifier.testTag("continue_button")) {
                    Text("Далее")
                }
            }
        } ?: Button(
            onClick = onCheck,
            enabled = (question?.let { q ->
                state.selectedAnswers[q.id]?.isNotEmpty() == true || state.selectedMatches[q.id]?.isNotEmpty() == true
            } == true),
            modifier = Modifier.testTag("check_button")
        ) {
            Text("Проверить")
        }
    }
}
