package com.fino.app.data.repository

import com.fino.app.data.local.dao.UserStatsDao
import com.fino.app.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class UserStatsRepositoryTest {

    private lateinit var mockUserStatsDao: UserStatsDao
    private lateinit var repository: UserStatsRepository

    @Before
    fun setup() {
        mockUserStatsDao = mock()
        repository = UserStatsRepository(mockUserStatsDao)
    }

    // Test 1: getUserStats returns null when no stats exist
    @Test
    fun `getUserStats returns null when no stats exist`(): Unit = runBlocking {
        // Given
        whenever(mockUserStatsDao.getUserStats()).thenReturn(null)

        // When
        val result = repository.getUserStats()

        // Then
        assertNull(result)
        verify(mockUserStatsDao).getUserStats()
    }

    // Test 2: getUserStats returns mapped domain object when stats exist
    @Test
    fun `getUserStats returns mapped domain object when stats exist`(): Unit = runBlocking {
        // Given
        val entity = UserStatsEntity(
            id = 1,
            currentStreak = 5,
            longestStreak = 10,
            totalTransactionsLogged = 50,
            totalXp = 500,
            currentLevel = 3,
            lastActiveDate = LocalDate.of(2024, 1, 15).toEpochDay() * 86400000,
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStats()).thenReturn(entity)

        // When
        val result = repository.getUserStats()

        // Then
        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals(5, result?.currentStreak)
        assertEquals(10, result?.longestStreak)
        assertEquals(50, result?.totalTransactionsLogged)
        assertEquals(500, result?.totalXp)
        assertEquals(3, result?.currentLevel)
    }

    // Test 3: getUserStatsFlow returns flow of mapped domain object
    @Test
    fun `getUserStatsFlow returns flow of mapped domain object`(): Unit = runBlocking {
        // Given
        val entity = UserStatsEntity(
            id = 1,
            currentStreak = 3,
            longestStreak = 7,
            totalTransactionsLogged = 25,
            totalXp = 250,
            currentLevel = 2,
            lastActiveDate = null,
            createdAt = System.currentTimeMillis()
        )
        whenever(mockUserStatsDao.getUserStatsFlow()).thenReturn(flowOf(entity))

        // When
        val result = repository.getUserStatsFlow().first()

        // Then
        assertNotNull(result)
        assertEquals(3, result?.currentStreak)
        assertEquals(250, result?.totalXp)
    }

    // Test 4: initializeUserStats creates default stats
    @Test
    fun `initializeUserStats creates default stats with id 1`(): Unit = runBlocking {
        // When
        repository.initializeUserStats()

        // Then
        verify(mockUserStatsDao).insert(argThat { entity ->
            entity.id == 1L &&
            entity.currentStreak == 0 &&
            entity.longestStreak == 0 &&
            entity.totalTransactionsLogged == 0 &&
            entity.totalXp == 0 &&
            entity.currentLevel == 1
        })
    }

    // Test 5: addXp delegates to dao
    @Test
    fun `addXp delegates to dao`(): Unit = runBlocking {
        // Given
        val xpToAdd = 50

        // When
        repository.addXp(xpToAdd)

        // Then
        verify(mockUserStatsDao).addXp(50)
    }

    // Test 6: updateStreak updates streak values
    @Test
    fun `updateStreak delegates to dao with correct parameters`(): Unit = runBlocking {
        // Given
        val currentStreak = 5
        val longestStreak = 10
        val lastActiveDate = LocalDate.of(2024, 1, 20)

        // When
        repository.updateStreak(currentStreak, longestStreak, lastActiveDate)

        // Then
        verify(mockUserStatsDao).updateStreak(
            eq(5),
            eq(10),
            any()
        )
    }

    // Test 7: updateLevel delegates to dao
    @Test
    fun `updateLevel delegates to dao`(): Unit = runBlocking {
        // Given
        val newLevel = 4

        // When
        repository.updateLevel(newLevel)

        // Then
        verify(mockUserStatsDao).updateLevel(4)
    }

    // Test 8: incrementTransactionCount delegates to dao
    @Test
    fun `incrementTransactionCount delegates to dao`(): Unit = runBlocking {
        // When
        repository.incrementTransactionCount()

        // Then
        verify(mockUserStatsDao).incrementTransactionCount()
    }

    // Test 9: getTotalXp returns value from dao
    @Test
    fun `getTotalXp returns value from dao`(): Unit = runBlocking {
        // Given
        whenever(mockUserStatsDao.getTotalXp()).thenReturn(1500)

        // When
        val result = repository.getTotalXp()

        // Then
        assertEquals(1500, result)
    }

    // Test 10: getTotalXp returns 0 when dao returns null
    @Test
    fun `getTotalXp returns 0 when dao returns null`(): Unit = runBlocking {
        // Given
        whenever(mockUserStatsDao.getTotalXp()).thenReturn(null)

        // When
        val result = repository.getTotalXp()

        // Then
        assertEquals(0, result)
    }
}
