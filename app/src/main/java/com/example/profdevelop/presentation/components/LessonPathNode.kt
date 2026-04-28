package com.example.profdevelop.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.profdevelop.R
import com.example.profdevelop.domain.model.Lesson
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandRouteDivider
import com.example.profdevelop.presentation.theme.BrandRouteLocked
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandWarm
import com.example.profdevelop.presentation.theme.BrandWarmSoft

/**
 * Дуо-style круглая нода урока. Один и тот же визуал на главной и на экране курса.
 */
@Composable
fun LessonPathNode(
    lesson: Lesson,
    index: Int,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val horizontalOffset = when (index % 5) {
        0 -> (-26).dp
        1 -> 40.dp
        2 -> 0.dp
        3 -> (-40).dp
        else -> 26.dp
    }
    val nodeColor = when {
        lesson.isCompleted -> BrandGreen
        lesson.isUnlocked -> BrandWarm
        else -> BrandRouteLocked
    }
    val haloColor = when {
        lesson.isCompleted -> BrandGreenSoft
        lesson.isUnlocked -> BrandWarmSoft
        else -> BrandSurface
    }
    val stepRotation = when (index % 5) {
        0 -> 28f; 1 -> -34f; 2 -> 32f; 3 -> -30f; else -> 26f
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.offset(x = horizontalOffset),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(haloColor, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(nodeColor, CircleShape)
                        .clickable(
                            enabled = lesson.isUnlocked || lesson.isCompleted,
                            onClick = onClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        lesson.isCompleted -> {
                            Image(
                                painter = painterResource(id = R.drawable.approved),
                                contentDescription = "Пройден",
                                modifier = Modifier.size(36.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                        lesson.isUnlocked -> {
                            Text(
                                text = "${lesson.orderIndex}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Заблокировано",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            if (!isLast) StepsConnector(rotation = stepRotation)
        }
    }
}

@Composable
private fun StepsConnector(rotation: Float) {
    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .rotate(rotation),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(BrandRouteDivider, CircleShape)
            )
        }
    }
}
