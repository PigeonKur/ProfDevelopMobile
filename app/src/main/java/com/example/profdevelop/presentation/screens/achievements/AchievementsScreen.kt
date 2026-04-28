package com.example.profdevelop.presentation.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.profdevelop.domain.model.Achievement
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandDanger
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandOutline
import com.example.profdevelop.presentation.theme.BrandSurface
import com.example.profdevelop.presentation.theme.BrandText
import com.example.profdevelop.presentation.theme.BrandWarmSoft

@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBackground)
    ) {
        Text(
            text = "Достижения",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = BrandText,
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, end = 20.dp)
        )
        Text(
            text = "Получай ачивки за прогресс — серии, XP, пройденные курсы",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandMuted,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 16.dp)
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = BrandGreen)
            }
            state.error != null -> Text(
                text = state.error!!,
                color = BrandDanger,
                modifier = Modifier.padding(20.dp)
            )
            state.items.isEmpty() -> EmptyAchievements()
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    AchievementCard(item)
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(item: Achievement) {
    val earned = !item.earnedAt.isNullOrBlank()
    val bg = if (earned) BrandWarmSoft else BrandSurface
    val accent = if (earned) BrandGreen else BrandMuted

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.icon?.takeIf { it.isNotBlank() } ?: "★",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BrandText,
                textAlign = TextAlign.Center
            )
            if (!item.description.isNullOrBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandMuted,
                    textAlign = TextAlign.Center
                )
            }
            if (earned) {
                Text(
                    text = "Получено",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyAchievements() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(12.dp))
        Text(
            "Достижений пока нет",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = BrandText
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Проходи уроки, продлевай серию — и они появятся здесь.",
            style = MaterialTheme.typography.bodyMedium,
            color = BrandMuted,
            textAlign = TextAlign.Center
        )
    }
}
