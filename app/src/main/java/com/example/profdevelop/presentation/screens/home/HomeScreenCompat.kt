package com.example.profdevelop.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

typealias NextLessonInfo = HomeNextLesson

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenLesson: (Int, String) -> Unit,
    onOpenCourse: (Int) -> Unit
) {
    if (state.isLoading) {
        CircularProgressIndicator(modifier = Modifier.testTag("home_loading"))
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = state.user?.fullName?.firstOrNull()?.toString() ?: "?", modifier = Modifier.testTag("avatar_initial"))
        Row {
            Text("${state.user?.streakDays ?: 0}")
            Spacer(Modifier.size(8.dp))
            Text("${state.user?.totalXp ?: 0}")
            Spacer(Modifier.size(8.dp))
            Text("${state.user?.level ?: 1}")
        }
        state.error?.let { Text(it) }
        state.nextLesson?.let {
            Text(
                text = it.lesson.title,
                modifier = Modifier
                    .testTag("continue_card")
                    .clickable { onOpenLesson(it.lesson.id, it.lesson.title) }
            )
        }
        state.chapters.forEach { chapter ->
            Text(chapter.course.title)
            chapter.lessons.forEach { lesson ->
                val tag = when {
                    lesson.isCompleted -> "lesson_node_completed_${lesson.id}"
                    !lesson.isUnlocked -> "lesson_node_locked_${lesson.id}"
                    else -> "lesson_node_${lesson.id}"
                }
                Text(
                    text = lesson.title,
                    modifier = Modifier
                        .testTag(tag)
                        .background(
                            when {
                                lesson.isCompleted -> Color(0xFF4CAF50)
                                !lesson.isUnlocked -> Color(0xFFBDBDBD)
                                else -> Color(0xFF90CAF9)
                            },
                            CircleShape
                        )
                        .padding(8.dp)
                        .then(if (lesson.isUnlocked) Modifier.clickable { onOpenLesson(lesson.id, lesson.title) } else Modifier)
                )
            }
        }
    }
}
