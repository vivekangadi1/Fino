package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.AchievementRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.Achievement
import com.fino.app.gamification.LevelCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for Rewards screen
 */
data class RewardsUiState(
    val currentLevel: Int = 1,
    val levelName: String = "Budget Beginner",
    val totalXp: Int = 0,
    val xpProgress: Float = 0f,
    val xpForNextLevel: Int = 100,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalAchievements: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val userStatsRepository: UserStatsRepository,
    private val achievementRepository: AchievementRepository,
    private val levelCalculator: LevelCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                userStatsRepository.getUserStatsFlow(),
                achievementRepository.getAllFlow()
            ) { userStats, achievements ->
                Pair(userStats, achievements)
            }.collect { (userStats, achievements) ->
                val totalXp = userStats?.totalXp ?: 0
                val currentLevel = userStats?.currentLevel ?: levelCalculator.calculateLevel(totalXp)
                val levelName = levelCalculator.getLevelName(currentLevel)
                val levelProgress = levelCalculator.getProgressToNextLevel(totalXp)

                val unlockedCount = achievements.count { it.unlockedAt != null }

                _uiState.update {
                    it.copy(
                        currentLevel = currentLevel,
                        levelName = levelName,
                        totalXp = totalXp,
                        xpProgress = levelProgress.progressPercent,
                        xpForNextLevel = levelProgress.xpForNextLevel,
                        currentStreak = userStats?.currentStreak ?: 0,
                        longestStreak = userStats?.longestStreak ?: 0,
                        achievements = achievements,
                        unlockedCount = unlockedCount,
                        totalAchievements = achievements.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadData()
    }
}
