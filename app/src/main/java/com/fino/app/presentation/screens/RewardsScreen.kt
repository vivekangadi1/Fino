package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.Achievement
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.components.primitives.HairlineDivider
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.RewardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: RewardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FinoColors.paper(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Milestones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = FinoColors.ink()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FinoColors.ink()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FinoColors.paper()
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                LevelHeader(
                    level = uiState.currentLevel,
                    levelName = uiState.levelName,
                    xp = uiState.totalXp,
                    progress = uiState.xpProgress,
                    xpForNextLevel = uiState.xpForNextLevel
                )
                HairlineDivider()
            }

            item {
                StatsStrip(
                    streak = uiState.currentStreak,
                    unlocked = uiState.unlockedCount,
                    total = uiState.totalAchievements
                )
                HairlineDivider()
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Eyebrow(text = "Achievements")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Earn these quietly, in your own time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinoColors.ink3()
                    )
                }
            }

            items(uiState.achievements) { achievement ->
                AchievementTile(achievement = achievement)
                HairlineDivider(modifier = Modifier.padding(start = 20.dp))
            }

            if (uiState.achievements.isEmpty()) {
                item {
                    Text(
                        text = "Earn XP by logging transactions and sticking with the habit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FinoColors.ink3(),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelHeader(
    level: Int,
    levelName: String,
    xp: Int,
    progress: Float,
    xpForNextLevel: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FinoColors.paper())
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Eyebrow(text = "Level $level")
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = levelName,
            style = SerifHero,
            color = FinoColors.ink()
        )
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = FinoColors.accentColor(),
            trackColor = FinoColors.line2(),
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$xp XP",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                color = FinoColors.ink2()
            )
            Text(
                text = "Next · $xpForNextLevel XP",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                color = FinoColors.ink3()
            )
        }
    }
}

@Composable
private fun StatsStrip(
    streak: Int,
    unlocked: Int,
    total: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        StatCell(
            label = "Day streak",
            value = "$streak",
            modifier = Modifier.weight(1f)
        )
        VerticalDivider()
        StatCell(
            label = "Unlocked",
            value = "$unlocked / $total",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Eyebrow(text = label)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = SerifMedium,
            color = FinoColors.ink()
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(FinoColors.line())
    )
}

@Composable
private fun AchievementTile(achievement: Achievement) {
    val isUnlocked = achievement.unlockedAt != null
    val alpha = if (isUnlocked) 1f else 0.55f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = achievement.emoji,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.bodyLarge,
                color = FinoColors.ink()
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = FinoColors.ink3()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isUnlocked) "+${achievement.xpReward} XP" else "Locked",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
            color = if (isUnlocked) FinoColors.accentInk() else FinoColors.ink3()
        )
    }
}
