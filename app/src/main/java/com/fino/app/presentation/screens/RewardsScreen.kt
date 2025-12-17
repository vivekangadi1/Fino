package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.Achievement
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.RewardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    viewModel: RewardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentRoute by remember { mutableStateOf("rewards") }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            FinoBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    currentRoute = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "cards" -> onNavigateToCards()
                        "analytics" -> onNavigateToAnalytics()
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header with Level Display
            item {
                RewardsHeader(
                    level = uiState.currentLevel,
                    levelName = uiState.levelName,
                    xp = uiState.totalXp,
                    progress = uiState.xpProgress
                )
            }

            // Streak Section
            item {
                StreakSection(currentStreak = uiState.currentStreak)
            }

            // Stats Row
            item {
                StatsRowSection(
                    unlockedCount = uiState.unlockedCount,
                    totalAchievements = uiState.totalAchievements,
                    totalXp = uiState.totalXp
                )
            }

            // Achievements Header
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Complete challenges to earn XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Achievements Grid
            item {
                AchievementsGrid(achievements = uiState.achievements)
            }

            // Coming Soon Teaser
            item {
                ComingSoonSection()
            }
        }
    }
}

@Composable
private fun RewardsHeader(
    level: Int,
    levelName: String,
    xp: Int,
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSurface, DarkBackground)
                )
            )
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Level Badge
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(FinoGradients.Gold),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LVL",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$level",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Level Name with Sparkle
            Row(verticalAlignment = Alignment.CenterVertically) {
                SparkleEffect()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = levelName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Accent
                )
                Spacer(modifier = Modifier.width(8.dp))
                SparkleEffect()
            }

            Spacer(modifier = Modifier.height(20.dp))

            // XP Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column {
                    AnimatedGradientProgress(
                        progress = progress,
                        gradient = FinoGradients.Gold,
                        backgroundColor = DarkSurfaceVariant,
                        height = 12.dp,
                        cornerRadius = 6.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AnimatedCounter(
                            targetValue = xp,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            suffix = " XP"
                        )
                        Text(
                            text = "Next: ${100 * (level)} XP",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakSection(currentStreak: Int) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        SlideInCard(delay = 100) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(FinoGradients.Fire)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fire Animation
                    FireAnimation(size = MaterialTheme.typography.displaySmall)

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedCounter(
                                targetValue = currentStreak,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Day Streak",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary.copy(alpha = 0.9f)
                            )
                        }
                        Text(
                            text = "Log a transaction daily to keep it going!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsRowSection(
    unlockedCount: Int,
    totalAchievements: Int,
    totalXp: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Achievements Stat
        SlideInCard(delay = 150, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$unlockedCount/$totalAchievements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Total XP Stat
        SlideInCard(delay = 200, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Stars,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedCounter(
                        targetValue = totalXp,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Total XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementsGrid(achievements: List<Achievement>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Use domain achievements if available, otherwise fall back to hardcoded list
        if (achievements.isNotEmpty()) {
            achievements.forEachIndexed { index, achievement ->
                SlideInCard(delay = 250 + (index * 50)) {
                    DomainAchievementRow(achievement)
                }
            }
        } else {
            modernAchievements.forEachIndexed { index, achievement ->
                SlideInCard(delay = 250 + (index * 50)) {
                    AchievementRow(achievement)
                }
            }
        }
    }
}

@Composable
private fun DomainAchievementRow(achievement: Achievement) {
    val isUnlocked = achievement.unlockedAt != null
    val alpha = if (isUnlocked) 1f else 0.6f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isUnlocked) {
                    FinoGradients.Primary
                } else {
                    Brush.linearGradient(listOf(DarkSurfaceVariant, DarkSurfaceHigh))
                }
            )
            .clickable { }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) {
                            DarkBackground.copy(alpha = 0.3f)
                        } else {
                            DarkSurfaceHigh
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.emoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked) TextPrimary else TextSecondary
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked) TextPrimary.copy(alpha = 0.7f) else TextTertiary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUnlocked) Accent else TextTertiary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Status Icon
            if (isUnlocked) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = "Locked",
                    tint = TextTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: ModernAchievement) {
    val alpha = if (achievement.isUnlocked) 1f else 0.6f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (achievement.isUnlocked) {
                    achievement.gradient
                } else {
                    Brush.linearGradient(listOf(DarkSurfaceVariant, DarkSurfaceHigh))
                }
            )
            .clickable { }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isUnlocked) {
                            DarkBackground.copy(alpha = 0.3f)
                        } else {
                            DarkSurfaceHigh
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.emoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (achievement.isUnlocked) TextPrimary else TextSecondary
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (achievement.isUnlocked) TextPrimary.copy(alpha = 0.7f) else TextTertiary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (achievement.isUnlocked) Accent else TextTertiary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Status Icon
            if (achievement.isUnlocked) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = "Locked",
                    tint = TextTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ComingSoonSection() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        SlideInCard(delay = 500) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.2f),
                                Secondary.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SparkleEffect()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leaderboards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SparkleEffect()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coming Soon",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Compete with friends and see who saves more!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Data classes
data class ModernAchievement(
    val emoji: String,
    val name: String,
    val description: String,
    val xpReward: Int,
    val isUnlocked: Boolean,
    val gradient: Brush
)

val modernAchievements = listOf(
    ModernAchievement(
        emoji = "🔥",
        name = "Getting Started",
        description = "Maintain a 3-day logging streak",
        xpReward = 50,
        isUnlocked = false,
        gradient = FinoGradients.Fire
    ),
    ModernAchievement(
        emoji = "⚡",
        name = "Week Warrior",
        description = "Maintain a 7-day logging streak",
        xpReward = 100,
        isUnlocked = false,
        gradient = FinoGradients.Primary
    ),
    ModernAchievement(
        emoji = "📝",
        name = "First Steps",
        description = "Log your first 10 transactions",
        xpReward = 25,
        isUnlocked = false,
        gradient = FinoGradients.Secondary
    ),
    ModernAchievement(
        emoji = "💯",
        name = "Century Tracker",
        description = "Log 100 transactions",
        xpReward = 200,
        isUnlocked = false,
        gradient = FinoGradients.Gold
    ),
    ModernAchievement(
        emoji = "💰",
        name = "Budget Beginner",
        description = "Create your first budget",
        xpReward = 50,
        isUnlocked = false,
        gradient = FinoGradients.Income
    ),
    ModernAchievement(
        emoji = "💳",
        name = "Card Keeper",
        description = "Add your first credit card",
        xpReward = 30,
        isUnlocked = false,
        gradient = FinoGradients.CardBlue
    )
)
