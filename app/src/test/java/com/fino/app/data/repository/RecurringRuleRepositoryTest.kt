package com.fino.app.data.repository

import com.fino.app.data.local.dao.RecurringRuleDao
import com.fino.app.data.local.entity.RecurringRuleEntity
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.domain.model.RecurringRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class RecurringRuleRepositoryTest {

    private lateinit var dao: RecurringRuleDao
    private lateinit var repository: RecurringRuleRepository

    @Before
    fun setup() {
        dao = mock()
        repository = RecurringRuleRepository(dao)
    }

    // Helper to convert LocalDate to epoch millis
    private fun LocalDate.toEpochMillis(): Long =
        this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // Helper to convert LocalDateTime to epoch millis
    private fun LocalDateTime.toEpochMillis(): Long =
        this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // ==================== Flow Tests ====================

    // Test 1: getActiveRulesFlow returns mapped domain models
    @Test
    fun `getActiveRulesFlow returns domain models from dao flow`() = runTest {
        val entity = createTestEntity(id = 1L, merchantPattern = "Netflix")
        whenever(dao.getActiveRulesFlow()).thenReturn(flowOf(listOf(entity)))

        val result = repository.getActiveRulesFlow().first()

        assertEquals(1, result.size)
        assertEquals("Netflix", result[0].merchantPattern)
        assertEquals(1L, result[0].id)
    }

    // Test 2: getActiveRulesFlow returns empty list when no rules
    @Test
    fun `getActiveRulesFlow returns empty list when no active rules`() = runTest {
        whenever(dao.getActiveRulesFlow()).thenReturn(flowOf(emptyList()))

        val result = repository.getActiveRulesFlow().first()

        assertTrue(result.isEmpty())
    }

    // ==================== Suspend Function Tests ====================

    // Test 3: getActiveRules returns all active rules
    @Test
    fun `getActiveRules returns all active rules as domain models`() = runTest {
        val entities = listOf(
            createTestEntity(id = 1L, merchantPattern = "Netflix"),
            createTestEntity(id = 2L, merchantPattern = "Spotify")
        )
        whenever(dao.getActiveRules()).thenReturn(entities)

        val result = repository.getActiveRules()

        assertEquals(2, result.size)
        assertEquals("Netflix", result[0].merchantPattern)
        assertEquals("Spotify", result[1].merchantPattern)
    }

    // Test 4: getById returns rule when found
    @Test
    fun `getById returns domain model when rule exists`() = runTest {
        val entity = createTestEntity(id = 42L, merchantPattern = "Amazon Prime")
        whenever(dao.getById(42L)).thenReturn(entity)

        val result = repository.getById(42L)

        assertNotNull(result)
        assertEquals(42L, result!!.id)
        assertEquals("Amazon Prime", result.merchantPattern)
    }

    // Test 5: getById returns null when not found
    @Test
    fun `getById returns null when rule does not exist`() = runTest {
        whenever(dao.getById(999L)).thenReturn(null)

        val result = repository.getById(999L)

        assertNull(result)
    }

    // Test 6: findByMerchantPattern returns matching rule
    @Test
    fun `findByMerchantPattern returns matching rule`() = runTest {
        val entity = createTestEntity(merchantPattern = "NETFLIX")
        whenever(dao.findByMerchantPattern("%NETFLIX%")).thenReturn(entity)

        val result = repository.findByMerchantPattern("NETFLIX")

        assertNotNull(result)
        assertEquals("NETFLIX", result!!.merchantPattern)
    }

    // Test 7: findByMerchantPattern returns null when no match
    @Test
    fun `findByMerchantPattern returns null when no match found`() = runTest {
        whenever(dao.findByMerchantPattern("%UNKNOWN%")).thenReturn(null)

        val result = repository.findByMerchantPattern("UNKNOWN")

        assertNull(result)
    }

    // ==================== Date Range Query Tests ====================

    // Test 8: getUpcomingRules returns rules in date range
    @Test
    fun `getUpcomingRules returns rules within date range`() = runTest {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val entity = createTestEntity(
            nextExpected = LocalDate.of(2024, 1, 15).toEpochMillis()
        )
        whenever(dao.getUpcomingRules(startDate.toEpochMillis(), endDate.toEpochMillis()))
            .thenReturn(listOf(entity))

        val result = repository.getUpcomingRules(startDate, endDate)

        assertEquals(1, result.size)
        assertEquals(LocalDate.of(2024, 1, 15), result[0].nextExpected)
    }

    // Test 9: getUpcomingRules returns empty for no rules in range
    @Test
    fun `getUpcomingRules returns empty list when no rules in range`() = runTest {
        val startDate = LocalDate.of(2024, 2, 1)
        val endDate = LocalDate.of(2024, 2, 28)
        whenever(dao.getUpcomingRules(startDate.toEpochMillis(), endDate.toEpochMillis()))
            .thenReturn(emptyList())

        val result = repository.getUpcomingRules(startDate, endDate)

        assertTrue(result.isEmpty())
    }

    // ==================== CRUD Operation Tests ====================

    // Test 10: insert creates new rule and returns ID
    @Test
    fun `insert creates rule and returns generated id`() = runTest {
        val rule = createTestRule(id = 0L)
        whenever(dao.insert(any())).thenReturn(100L)

        val resultId = repository.insert(rule)

        assertEquals(100L, resultId)
        verify(dao).insert(argThat { merchantPattern == rule.merchantPattern })
    }

    // Test 11: update modifies existing rule
    @Test
    fun `update calls dao update with mapped entity`() = runTest {
        val rule = createTestRule(id = 50L, merchantPattern = "Updated Netflix")

        repository.update(rule)

        verify(dao).update(argThat {
            id == 50L && merchantPattern == "Updated Netflix"
        })
    }

    // Test 12: delete removes rule
    @Test
    fun `delete calls dao delete with mapped entity`() = runTest {
        val rule = createTestRule(id = 25L)

        repository.delete(rule)

        verify(dao).delete(argThat { id == 25L })
    }

    // Test 13: recordOccurrence updates rule state
    @Test
    fun `recordOccurrence updates last occurrence and next expected`() = runTest {
        val date = LocalDate.of(2024, 1, 15)
        val nextDate = LocalDate.of(2024, 2, 15)

        repository.recordOccurrence(10L, date, nextDate)

        verify(dao).recordOccurrence(
            eq(10L),
            eq(date.toEpochMillis()),
            eq(nextDate.toEpochMillis())
        )
    }

    // ==================== Entity-Domain Mapping Tests ====================

    // Test 14: Entity with all fields maps correctly to domain
    @Test
    fun `entity with all fields maps correctly to domain model`() = runTest {
        val lastOccurrence = LocalDate.of(2024, 1, 1)
        val nextExpected = LocalDate.of(2024, 2, 1)
        val createdAt = LocalDateTime.of(2023, 12, 1, 10, 30)

        val entity = RecurringRuleEntity(
            id = 1L,
            merchantPattern = "Test Merchant",
            categoryId = 5L,
            expectedAmount = 999.99,
            amountVariance = 0.15f,
            frequency = RecurringFrequency.MONTHLY,
            dayOfPeriod = 15,
            lastOccurrence = lastOccurrence.toEpochMillis(),
            nextExpected = nextExpected.toEpochMillis(),
            occurrenceCount = 3,
            isActive = true,
            isUserConfirmed = true,
            createdAt = createdAt.toEpochMillis()
        )
        whenever(dao.getById(1L)).thenReturn(entity)

        val result = repository.getById(1L)

        assertNotNull(result)
        assertEquals(1L, result!!.id)
        assertEquals("Test Merchant", result.merchantPattern)
        assertEquals(5L, result.categoryId)
        assertEquals(999.99, result.expectedAmount, 0.01)
        assertEquals(0.15f, result.amountVariance)
        assertEquals(RecurringFrequency.MONTHLY, result.frequency)
        assertEquals(15, result.dayOfPeriod)
        assertEquals(lastOccurrence, result.lastOccurrence)
        assertEquals(nextExpected, result.nextExpected)
        assertEquals(3, result.occurrenceCount)
        assertTrue(result.isActive)
        assertTrue(result.isUserConfirmed)
    }

    // Test 15: Entity with null dates maps correctly
    @Test
    fun `entity with null dates maps to domain with null dates`() = runTest {
        val entity = RecurringRuleEntity(
            id = 2L,
            merchantPattern = "New Rule",
            categoryId = 1L,
            expectedAmount = 500.0,
            frequency = RecurringFrequency.WEEKLY,
            lastOccurrence = null,
            nextExpected = null,
            createdAt = System.currentTimeMillis()
        )
        whenever(dao.getById(2L)).thenReturn(entity)

        val result = repository.getById(2L)

        assertNotNull(result)
        assertNull(result!!.lastOccurrence)
        assertNull(result.nextExpected)
    }

    // Test 16: Domain model maps correctly to entity for insert
    @Test
    fun `domain model maps correctly to entity for insertion`() = runTest {
        val rule = RecurringRule(
            id = 0L,
            merchantPattern = "New Subscription",
            categoryId = 3L,
            expectedAmount = 1500.0,
            amountVariance = 0.05f,
            frequency = RecurringFrequency.YEARLY,
            dayOfPeriod = 25,
            lastOccurrence = null,
            nextExpected = LocalDate.of(2025, 1, 25),
            occurrenceCount = 0,
            isActive = true,
            isUserConfirmed = false,
            createdAt = LocalDateTime.now()
        )
        whenever(dao.insert(any())).thenReturn(1L)

        repository.insert(rule)

        verify(dao).insert(argThat {
            merchantPattern == "New Subscription" &&
            categoryId == 3L &&
            expectedAmount == 1500.0 &&
            frequency == RecurringFrequency.YEARLY &&
            dayOfPeriod == 25
        })
    }

    // ==================== Edge Case Tests ====================

    // Test 17: Multiple rules with same merchant pattern different IDs
    @Test
    fun `handles multiple rules with different frequencies correctly`() = runTest {
        val entities = listOf(
            createTestEntity(id = 1L, merchantPattern = "Netflix", frequency = RecurringFrequency.MONTHLY),
            createTestEntity(id = 2L, merchantPattern = "Netflix", frequency = RecurringFrequency.YEARLY)
        )
        whenever(dao.getActiveRules()).thenReturn(entities)

        val result = repository.getActiveRules()

        assertEquals(2, result.size)
        assertEquals(RecurringFrequency.MONTHLY, result[0].frequency)
        assertEquals(RecurringFrequency.YEARLY, result[1].frequency)
    }

    // Test 18: Rule with zero expected amount
    @Test
    fun `handles rule with zero expected amount`() = runTest {
        val entity = createTestEntity(expectedAmount = 0.0)
        whenever(dao.getActiveRules()).thenReturn(listOf(entity))

        val result = repository.getActiveRules()

        assertEquals(0.0, result[0].expectedAmount, 0.01)
    }

    // Test 19: Rule with maximum variance
    @Test
    fun `handles rule with high amount variance`() = runTest {
        val entity = createTestEntity(amountVariance = 1.0f) // 100% variance
        whenever(dao.getActiveRules()).thenReturn(listOf(entity))

        val result = repository.getActiveRules()

        assertEquals(1.0f, result[0].amountVariance)
    }

    // Test 20: getActiveRuleCount returns correct count
    @Test
    fun `getActiveRuleCount returns count from dao`() = runTest {
        whenever(dao.getActiveRuleCount()).thenReturn(7)

        val count = repository.getActiveRuleCount()

        assertEquals(7, count)
    }

    // ==================== Helper Functions ====================

    private fun createTestEntity(
        id: Long = 1L,
        merchantPattern: String = "Test Merchant",
        categoryId: Long = 1L,
        expectedAmount: Double = 1000.0,
        amountVariance: Float = 0.1f,
        frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
        dayOfPeriod: Int? = 15,
        lastOccurrence: Long? = null,
        nextExpected: Long? = LocalDate.now().plusDays(7).toEpochMillis(),
        occurrenceCount: Int = 1,
        isActive: Boolean = true,
        isUserConfirmed: Boolean = true,
        createdAt: Long = System.currentTimeMillis()
    ): RecurringRuleEntity {
        return RecurringRuleEntity(
            id = id,
            merchantPattern = merchantPattern,
            categoryId = categoryId,
            expectedAmount = expectedAmount,
            amountVariance = amountVariance,
            frequency = frequency,
            dayOfPeriod = dayOfPeriod,
            lastOccurrence = lastOccurrence,
            nextExpected = nextExpected,
            occurrenceCount = occurrenceCount,
            isActive = isActive,
            isUserConfirmed = isUserConfirmed,
            createdAt = createdAt
        )
    }

    private fun createTestRule(
        id: Long = 1L,
        merchantPattern: String = "Test Merchant",
        categoryId: Long = 1L,
        expectedAmount: Double = 1000.0,
        amountVariance: Float = 0.1f,
        frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
        dayOfPeriod: Int? = 15,
        lastOccurrence: LocalDate? = null,
        nextExpected: LocalDate? = LocalDate.now().plusDays(7),
        occurrenceCount: Int = 1,
        isActive: Boolean = true,
        isUserConfirmed: Boolean = true,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): RecurringRule {
        return RecurringRule(
            id = id,
            merchantPattern = merchantPattern,
            categoryId = categoryId,
            expectedAmount = expectedAmount,
            amountVariance = amountVariance,
            frequency = frequency,
            dayOfPeriod = dayOfPeriod,
            lastOccurrence = lastOccurrence,
            nextExpected = nextExpected,
            occurrenceCount = occurrenceCount,
            isActive = isActive,
            isUserConfirmed = isUserConfirmed,
            createdAt = createdAt
        )
    }
}
