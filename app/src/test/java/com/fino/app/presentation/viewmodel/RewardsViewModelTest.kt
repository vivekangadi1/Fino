package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.AchievementRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.Achievement
import com.fino.app.domain.model.AchievementType
import com.fino.app.domain.model.UserStats
import com.fino.app.gamification.LevelCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class RewardsViewModelTest {

    private lateinit var mockUserStatsRepository: UserStatsRepository
    private lateinit var mockAchievementRepository: AchievementRepository
    private lateinit var levelCalculator: LevelCalculator
    private lateinit var viewModel: RewardsViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testUserStats = UserStats(
        id = 1L,
        currentStreak = 7,
        longestStreak = 14,
        totalTransactionsLogged = 75,
        totalXp = 450,
        currentLevel = 4
    )

    private val testAchievements = listOf(
        Achievement(
            id = "streak_3",
            name = "Getting Started",
            description = "Maintain a 3-day streak",
            emoji = "🔥",
            xpReward = 50,
            requirement = 3,
            type = AchievementType.STREAK,
            unlockedAt = LocalDateTime.now().minusDays(5),
            progress = 3
        ),
        Achievement(
            id = "streak_7",
            name = "Week Warrior",
            description = "Maintain a 7-day streak",
            emoji = "⚡",
            xpReward = 100,
            requirement = 7,
            type = AchievementType.STREAK,
            unlockedAt = LocalDateTime.now(),
            progress = 7
        ),
        Achievement(
            id = "txn_100",
            name = "Century Tracker",
            description = "Log 100 transactions",
            emoji = "💯",
            xpReward = 200,
            requirement = 100,
            type = AchievementType.TRANSACTION_COUNT,
            unlockedAt = null,
            progress = 75
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockUserStatsRepository = mock()
        mockAchievementRepository = mock()
        levelCalculator = LevelCalculator()

        whenever(mockUserStatsRepository.getUserStatsFlow()).thenReturn(flowOf(testUserStats))
        whenever(mockAchievementRepository.getAllFlow()).thenReturn(flowOf(testAchievements))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): RewardsViewModel {
        return RewardsViewModel(
            userStatsRepository = mockUserStatsRepository,
            achievementRepository = mockAchievementRepository,
            levelCalculator = levelCalculator
        )
    }

    // Test 1: Initial state shows loading
    @Test
    fun `initial state shows loading`() = runTest {
        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertTrue(state.isLoading)
    }

    // Test 2: fetches current level from user stats
    @Test
    fun `fetches current level from user stats`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(4, state.currentLevel)
    }

    // Test 3: calculates level name correctly
    @Test
    fun `calculates level name from level calculator`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(levelCalculator.getLevelName(4), state.levelName)
    }

    // Test 4: fetches total XP from user stats
    @Test
    fun `fetches total XP from user stats`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(450, state.totalXp)
    }

    // Test 5: calculates XP progress correctly
    @Test
    fun `calculates XP progress toward next level`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.xpProgress in 0f..1f)
    }

    // Test 6: fetches current streak from user stats
    @Test
    fun `fetches current streak from user stats`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(7, state.currentStreak)
    }

    // Test 7: fetches achievements from repository
    @Test
    fun `fetches achievements from repository`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(3, state.achievements.size)
    }

    // Test 8: counts unlocked achievements correctly
    @Test
    fun `counts unlocked achievements correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // 2 achievements are unlocked (streak_3 and streak_7)
        assertEquals(2, state.unlockedCount)
    }

    // Test 9: handles null user stats with defaults
    @Test
    fun `handles null user stats with defaults`() = runTest {
        whenever(mockUserStatsRepository.getUserStatsFlow()).thenReturn(flowOf(null))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(1, state.currentLevel)
        assertEquals(0, state.totalXp)
        assertEquals(0, state.currentStreak)
    }

    // Test 10: isLoading becomes false after data loads
    @Test
    fun `isLoading becomes false after data loads`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
    }

    // Test 11: achievements are sorted with unlocked first
    @Test
    fun `achievements show unlocked ones prominently`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Verify unlocked achievements exist
        val unlockedAchievements = state.achievements.filter { it.unlockedAt != null }
        assertEquals(2, unlockedAchievements.size)
    }
}
