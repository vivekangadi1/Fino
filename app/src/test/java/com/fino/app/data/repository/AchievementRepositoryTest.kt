package com.fino.app.data.repository

import com.fino.app.data.local.dao.AchievementDao
import com.fino.app.data.local.entity.AchievementEntity
import com.fino.app.domain.model.Achievement
import com.fino.app.domain.model.AchievementType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class AchievementRepositoryTest {

    private lateinit var mockAchievementDao: AchievementDao
    private lateinit var repository: AchievementRepository

    @Before
    fun setup() {
        mockAchievementDao = mock()
        repository = AchievementRepository(mockAchievementDao)
    }

    // Test 1: getAll returns mapped domain objects
    @Test
    fun `getAll returns list of mapped achievements`() = runBlocking {
        // Given
        val entities = listOf(
            AchievementEntity(
                id = "streak_3",
                name = "Getting Started",
                description = "Maintain a 3-day streak",
                emoji = "🔥",
                xpReward = 50,
                requirement = 3,
                type = AchievementType.STREAK,
                unlockedAt = null,
                progress = 1
            ),
            AchievementEntity(
                id = "txn_10",
                name = "First Steps",
                description = "Log 10 transactions",
                emoji = "📝",
                xpReward = 25,
                requirement = 10,
                type = AchievementType.TRANSACTION_COUNT,
                unlockedAt = null,
                progress = 5
            )
        )
        whenever(mockAchievementDao.getAll()).thenReturn(entities)

        // When
        val result = repository.getAll()

        // Then
        assertEquals(2, result.size)
        assertEquals("Getting Started", result[0].name)
        assertEquals(AchievementType.STREAK, result[0].type)
        assertEquals("First Steps", result[1].name)
    }

    // Test 2: getAllFlow returns flow of mapped achievements
    @Test
    fun `getAllFlow returns flow of mapped achievements`() = runBlocking {
        // Given
        val entities = listOf(
            AchievementEntity(
                id = "streak_7",
                name = "Week Warrior",
                description = "7-day streak",
                emoji = "⚡",
                xpReward = 100,
                requirement = 7,
                type = AchievementType.STREAK,
                unlockedAt = null,
                progress = 0
            )
        )
        whenever(mockAchievementDao.getAllFlow()).thenReturn(flowOf(entities))

        // When
        val result = repository.getAllFlow().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Week Warrior", result[0].name)
    }

    // Test 3: getUnlocked returns only unlocked achievements
    @Test
    fun `getUnlocked returns only unlocked achievements`() = runBlocking {
        // Given
        val unlockedEntities = listOf(
            AchievementEntity(
                id = "streak_3",
                name = "Getting Started",
                description = "3-day streak",
                emoji = "🔥",
                xpReward = 50,
                requirement = 3,
                type = AchievementType.STREAK,
                unlockedAt = System.currentTimeMillis(),
                progress = 3
            )
        )
        whenever(mockAchievementDao.getUnlocked()).thenReturn(unlockedEntities)

        // When
        val result = repository.getUnlocked()

        // Then
        assertEquals(1, result.size)
        assertNotNull(result[0].unlockedAt)
    }

    // Test 4: getLocked returns only locked achievements
    @Test
    fun `getLocked returns only locked achievements`() = runBlocking {
        // Given
        val lockedEntities = listOf(
            AchievementEntity(
                id = "txn_100",
                name = "Century Tracker",
                description = "Log 100 transactions",
                emoji = "💯",
                xpReward = 200,
                requirement = 100,
                type = AchievementType.TRANSACTION_COUNT,
                unlockedAt = null,
                progress = 25
            )
        )
        whenever(mockAchievementDao.getLocked()).thenReturn(lockedEntities)

        // When
        val result = repository.getLocked()

        // Then
        assertEquals(1, result.size)
        assertNull(result[0].unlockedAt)
    }

    // Test 5: unlock sets timestamp
    @Test
    fun `unlock delegates to dao with current timestamp`() = runBlocking {
        // Given
        val achievementId = "streak_7"

        // When
        repository.unlock(achievementId)

        // Then
        verify(mockAchievementDao).unlock(eq(achievementId), any())
    }

    // Test 6: updateProgress updates progress value
    @Test
    fun `updateProgress delegates to dao`() = runBlocking {
        // Given
        val achievementId = "txn_100"
        val progress = 50

        // When
        repository.updateProgress(achievementId, progress)

        // Then
        verify(mockAchievementDao).updateProgress("txn_100", 50)
    }

    // Test 7: getUnlockedCount returns count from dao
    @Test
    fun `getUnlockedCount returns count from dao`() = runBlocking {
        // Given
        whenever(mockAchievementDao.getUnlockedCount()).thenReturn(5)

        // When
        val result = repository.getUnlockedCount()

        // Then
        assertEquals(5, result)
    }

    // Test 8: getById returns mapped achievement
    @Test
    fun `getById returns mapped achievement when exists`() = runBlocking {
        // Given
        val entity = AchievementEntity(
            id = "budget_1",
            name = "Budget Beginner",
            description = "Create your first budget",
            emoji = "💰",
            xpReward = 50,
            requirement = 1,
            type = AchievementType.BUDGET,
            unlockedAt = null,
            progress = 0
        )
        whenever(mockAchievementDao.getById("budget_1")).thenReturn(entity)

        // When
        val result = repository.getById("budget_1")

        // Then
        assertNotNull(result)
        assertEquals("Budget Beginner", result?.name)
        assertEquals(AchievementType.BUDGET, result?.type)
    }

    // Test 9: getById returns null when not exists
    @Test
    fun `getById returns null when achievement does not exist`() = runBlocking {
        // Given
        whenever(mockAchievementDao.getById("nonexistent")).thenReturn(null)

        // When
        val result = repository.getById("nonexistent")

        // Then
        assertNull(result)
    }

    // Test 10: seedDefaultAchievements inserts all default achievements
    @Test
    fun `seedDefaultAchievements inserts achievements via dao`() = runBlocking {
        // When
        repository.seedDefaultAchievements()

        // Then
        verify(mockAchievementDao).insertAll(argThat { achievements ->
            achievements.isNotEmpty() &&
            achievements.any { it.id == "streak_3" } &&
            achievements.any { it.id == "streak_7" } &&
            achievements.any { it.id == "txn_10" } &&
            achievements.any { it.id == "txn_100" }
        })
    }

    // Test 11: Entity to domain mapping preserves all fields including unlockedAt
    @Test
    fun `entity to domain mapping preserves unlocked timestamp`() = runBlocking {
        // Given
        val timestamp = System.currentTimeMillis()
        val entity = AchievementEntity(
            id = "test_achievement",
            name = "Test",
            description = "Test description",
            emoji = "🎯",
            xpReward = 100,
            requirement = 5,
            type = AchievementType.SPECIAL,
            unlockedAt = timestamp,
            progress = 5
        )
        whenever(mockAchievementDao.getById("test_achievement")).thenReturn(entity)

        // When
        val result = repository.getById("test_achievement")

        // Then
        assertNotNull(result)
        assertNotNull(result?.unlockedAt)
        assertEquals(5, result?.progress)
    }
}
