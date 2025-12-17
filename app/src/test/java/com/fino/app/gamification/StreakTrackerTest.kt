package com.fino.app.gamification

import com.fino.app.data.local.dao.UserStatsDao
import com.fino.app.data.local.entity.UserStatsEntity
import com.fino.app.util.DateUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate

class StreakTrackerTest {

    private lateinit var streakTracker: StreakTracker
    private lateinit var mockUserStatsDao: UserStatsDao

    @Before
    fun setup() {
        mockUserStatsDao = mock()
        streakTracker = StreakTracker(mockUserStatsDao)
    }

    @Test
    fun `first activity - starts streak at 1`() = runBlocking {
        val stats = UserStatsEntity(
            currentStreak = 0,
            longestStreak = 0,
            lastActiveDate = null,
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)

        val newStreak = streakTracker.recordActivity(LocalDate.now())

        assertEquals(1, newStreak)
    }

    @Test
    fun `consecutive day - increases streak`() = runBlocking {
        val yesterday = LocalDate.now().minusDays(1)
        val stats = UserStatsEntity(
            currentStreak = 5,
            longestStreak = 5,
            lastActiveDate = DateUtils.toEpochMillis(yesterday),
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)

        val newStreak = streakTracker.recordActivity(LocalDate.now())

        assertEquals(6, newStreak)
    }

    @Test
    fun `same day activity - does not increase streak`() = runBlocking {
        val today = LocalDate.now()
        val stats = UserStatsEntity(
            currentStreak = 5,
            longestStreak = 10,
            lastActiveDate = DateUtils.toEpochMillis(today),
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)

        val newStreak = streakTracker.recordActivity(today)

        assertEquals(5, newStreak)
    }

    @Test
    fun `missed one day - resets streak to 1`() = runBlocking {
        val twoDaysAgo = LocalDate.now().minusDays(2)
        val stats = UserStatsEntity(
            currentStreak = 10,
            longestStreak = 15,
            lastActiveDate = DateUtils.toEpochMillis(twoDaysAgo),
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)

        val newStreak = streakTracker.recordActivity(LocalDate.now())

        assertEquals(1, newStreak)
    }

    @Test
    fun `new streak exceeds longest - updates longest streak`() = runBlocking {
        val yesterday = LocalDate.now().minusDays(1)
        val stats = UserStatsEntity(
            currentStreak = 15,
            longestStreak = 15,
            lastActiveDate = DateUtils.toEpochMillis(yesterday),
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)

        streakTracker.recordActivity(LocalDate.now())

        verify(mockUserStatsDao).updateStreak(
            streak = eq(16),
            longestStreak = eq(16),
            lastActiveDate = any()
        )
    }

    @Test
    fun `streak below longest - does not update longest`() = runBlocking {
        val yesterday = LocalDate.now().minusDays(1)
        val stats = UserStatsEntity(
            currentStreak = 5,
            longestStreak = 30,
            lastActiveDate = DateUtils.toEpochMillis(yesterday),
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)

        streakTracker.recordActivity(LocalDate.now())

        verify(mockUserStatsDao).updateStreak(
            streak = eq(6),
            longestStreak = eq(30),
            lastActiveDate = any()
        )
    }
}
