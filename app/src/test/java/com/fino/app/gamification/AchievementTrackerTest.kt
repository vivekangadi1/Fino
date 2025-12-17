package com.fino.app.gamification

import com.fino.app.data.local.dao.AchievementDao
import com.fino.app.data.local.dao.UserStatsDao
import com.fino.app.data.local.entity.UserStatsEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class AchievementTrackerTest {

    private lateinit var tracker: AchievementTracker
    private lateinit var mockAchievementDao: AchievementDao
    private lateinit var mockUserStatsDao: UserStatsDao

    @Before
    fun setup() {
        mockAchievementDao = mock()
        mockUserStatsDao = mock()
        tracker = AchievementTracker(mockAchievementDao, mockUserStatsDao)
    }

    @Test
    fun `unlock streak_7 at 7 day streak`() = runBlocking {
        val stats = UserStatsEntity(
            currentStreak = 7,
            longestStreak = 7,
            totalTransactionsLogged = 50,
            totalXp = 100,
            currentLevel = 2,
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        // Order matters: specific stub after general stub
        whenever(mockAchievementDao.isUnlocked(any())).thenReturn(true)
        whenever(mockAchievementDao.isUnlocked("streak_7")).thenReturn(false)
        whenever(mockAchievementDao.isUnlocked("streak_3")).thenReturn(true) // Already unlocked
        whenever(mockUserStatsDao.getTotalXp()).thenReturn(100)

        val unlocked = tracker.checkAndUnlock()

        assertTrue(unlocked.any { it.id == "streak_7" })
        verify(mockAchievementDao).unlock(eq("streak_7"), any())
    }

    @Test
    fun `do not unlock already unlocked achievement`() = runBlocking {
        val stats = UserStatsEntity(
            currentStreak = 7,
            longestStreak = 7,
            totalTransactionsLogged = 50,
            totalXp = 100,
            currentLevel = 2,
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        whenever(mockAchievementDao.isUnlocked("streak_7")).thenReturn(true)
        whenever(mockAchievementDao.isUnlocked(any())).thenReturn(true)

        val unlocked = tracker.checkAndUnlock()

        assertFalse(unlocked.any { it.id == "streak_7" })
    }

    @Test
    fun `unlock txn_100 at 100 transactions`() = runBlocking {
        val stats = UserStatsEntity(
            currentStreak = 5,
            longestStreak = 10,
            totalTransactionsLogged = 100,
            totalXp = 200,
            currentLevel = 3,
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        // Order matters: specific stub after general stub
        whenever(mockAchievementDao.isUnlocked(any())).thenReturn(true)
        whenever(mockAchievementDao.isUnlocked("txn_100")).thenReturn(false)
        whenever(mockAchievementDao.isUnlocked("txn_10")).thenReturn(true)
        whenever(mockAchievementDao.isUnlocked("txn_50")).thenReturn(true)
        whenever(mockUserStatsDao.getTotalXp()).thenReturn(200)

        val unlocked = tracker.checkAndUnlock()

        assertTrue(unlocked.any { it.id == "txn_100" })
    }

    @Test
    fun `award XP when achievement unlocked`() = runBlocking {
        val stats = UserStatsEntity(
            currentStreak = 7,
            longestStreak = 7,
            totalTransactionsLogged = 50,
            totalXp = 100,
            currentLevel = 2,
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        // Order matters: specific stub after general stub
        whenever(mockAchievementDao.isUnlocked(any())).thenReturn(true)
        whenever(mockAchievementDao.isUnlocked("streak_7")).thenReturn(false)
        whenever(mockAchievementDao.isUnlocked("streak_3")).thenReturn(true)
        whenever(mockUserStatsDao.getTotalXp()).thenReturn(100)

        tracker.checkAndUnlock()

        // streak_7 gives 50 XP
        verify(mockUserStatsDao).addXp(50)
    }
}
