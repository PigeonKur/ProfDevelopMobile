package com.example.profdevelop.presentation.screens.lesson

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandWarm
import com.example.profdevelop.presentation.theme.BrandWarmSoft

/**
 * Двухколоночная борда «соответствий»: слева — пункты, справа — варианты ответа,
 * перемешанные так, чтобы порядок не совпадал с левой колонкой.
 */
@Composable
internal fun MatchingBoard(
    question: Question,
    selectedMatches: Map<Int, Int>,
    onSelect: (Int, Int) -> Unit
) {
    var activeLeftId by remember(question.id) {
        mutableIntStateOf(question.matchingPairs.firstOrNull()?.id ?: -1)
    }
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
