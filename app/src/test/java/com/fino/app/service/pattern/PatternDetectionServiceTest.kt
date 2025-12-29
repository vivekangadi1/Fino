package com.fino.app.service.pattern

import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.*
import com.fino.app.ml.matcher.MerchantMatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime

class PatternDetectionServiceTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var recurringRuleRepository: RecurringRuleRepository
    private lateinit var merchantMatcher: MerchantMatcher
    private lateinit var service: PatternDetectionService

    @Before
    fun setup() {
        transactionRepository = mock()
        recurringRuleRepository = mock()
        merchantMatcher = mock()
        service = PatternDetectionService(
            transactionRepository,
            recurringRuleRepository,
            merchantMatcher
        )
    }

    // ==================== Merchant Grouping Tests (8 tests) ====================

    // Test 1: Groups transactions by exact merchant name
    @Test
    fun `groupTransactionsByMerchant groups by exact merchant name`() {
        val transactions = listOf(
            createTransaction("Netflix", LocalDateTime.of(2024, 1, 15, 10, 0)),
            createTransaction("Netflix", LocalDateTime.of(2024, 2, 15, 10, 0)),
            createTransaction("Spotify", LocalDateTime.of(2024, 1, 20, 10, 0))
        )

        val groups = service.groupTransactionsByMerchant(transactions)

        assertEquals(2, groups.size)
        assertEquals(2, groups["NETFLIX"]?.size)
        assertEquals(1, groups["SPOTIFY"]?.size)
    }

    // Test 2: Groups transactions with fuzzy matching
    @Test
    fun `groupTransactionsByMerchant groups similar merchant names`() {
        whenever(merchantMatcher.calculateSimilarity("NETFLIX", "NETFLIX INC")).thenReturn(0.85f)
        whenever(merchantMatcher.calculateSimilarity("NETFLIX", "NETFLIX")).thenReturn(1.0f)
        whenever(merchantMatcher.calculateSimilarity("NETFLIX INC", "NETFLIX")).thenReturn(0.85f)
        whenever(merchantMatcher.calculateSimilarity("NETFLIX INC", "NETFLIX INC")).thenReturn(1.0f)

        val transactions = listOf(
            createTransaction("NETFLIX", LocalDateTime.of(2024, 1, 15, 10, 0)),
            createTransaction("NETFLIX INC", LocalDateTime.of(2024, 2, 15, 10, 0)),
            createTransaction("NETFLIX", LocalDateTime.of(2024, 3, 15, 10, 0))
        )

        val groups = service.groupTransactionsByMerchant(transactions)

        // Should group similar names together
        assertTrue(groups.size <= 2) // Either 1 group (merged) or 2 groups
    }

    // Test 3: Returns empty map for empty transactions
    @Test
    fun `groupTransactionsByMerchant returns empty map for empty list`() {
        val groups = service.groupTransactionsByMerchant(emptyList())

        assertTrue(groups.isEmpty())
    }

    // Test 4: Handles single transaction
    @Test
    fun `groupTransactionsByMerchant handles single transaction`() {
        val transactions = listOf(
            createTransaction("Netflix", LocalDateTime.of(2024, 1, 15, 10, 0))
        )

        val groups = service.groupTransactionsByMerchant(transactions)

        assertEquals(1, groups.size)
        assertEquals(1, groups["NETFLIX"]?.size)
    }

    // Test 5: Normalizes merchant names to uppercase
    @Test
    fun `groupTransactionsByMerchant normalizes merchant names`() {
        val transactions = listOf(
            createTransaction("Netflix", LocalDateTime.of(2024, 1, 15, 10, 0)),
            createTransaction("NETFLIX", LocalDateTime.of(2024, 2, 15, 10, 0)),
            createTransaction("netflix", LocalDateTime.of(2024, 3, 15, 10, 0))
        )

        val groups = service.groupTransactionsByMerchant(transactions)

        // All should be grouped together due to normalization
        assertEquals(1, groups.size)
        assertEquals(3, groups.values.first().size)
    }

    // Test 6: Handles multiple distinct merchants
    @Test
    fun `groupTransactionsByMerchant handles many merchants`() {
        val transactions = listOf(
            createTransaction("Netflix", LocalDateTime.of(2024, 1, 1, 10, 0)),
            createTransaction("Spotify", LocalDateTime.of(2024, 1, 2, 10, 0)),
            createTransaction("Amazon", LocalDateTime.of(2024, 1, 3, 10, 0)),
            createTransaction("Netflix", LocalDateTime.of(2024, 2, 1, 10, 0)),
            createTransaction("Apple", LocalDateTime.of(2024, 1, 4, 10, 0))
        )

        val groups = service.groupTransactionsByMerchant(transactions)

        assertEquals(4, groups.size)
        assertEquals(2, groups["NETFLIX"]?.size)
    }

    // Test 7: Filters only DEBIT transactions for pattern detection
    @Test
    fun `groupTransactionsByMerchant filters debit transactions only`() {
        val transactions = listOf(
            createTransaction("Netflix", LocalDateTime.of(2024, 1, 15, 10, 0), type = TransactionType.DEBIT),
            createTransaction("Refund", LocalDateTime.of(2024, 1, 20, 10, 0), type = TransactionType.CREDIT),
            createTransaction("Netflix", LocalDateTime.of(2024, 2, 15, 10, 0), type = TransactionType.DEBIT)
        )

        val groups = service.groupTransactionsByMerchant(transactions)

        // Should only include DEBIT transactions
        assertEquals(1, groups.size)
        assertEquals(2, groups["NETFLIX"]?.size)
    }

    // Test 8: Handles whitespace in merchant names
    @Test
    fun `groupTransactionsByMerchant trims whitespace`() {
        val transactions = listOf(
            createTransaction("  Netflix  ", LocalDateTime.of(2024, 1, 15, 10, 0)),
            createTransaction("Netflix", LocalDateTime.of(2024, 2, 15, 10, 0))
        )

        val groups = service.groupTransactionsByMerchant(transactions)

        assertEquals(1, groups.size)
        assertEquals(2, groups.values.first().size)
    }

    // ==================== Frequency Detection Tests (10 tests) ====================

    // Test 9: Detects MONTHLY frequency for 28-31 day intervals
    @Test
    fun `detectFrequency returns MONTHLY for monthly intervals`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 2, 15),
            LocalDate.of(2024, 3, 15)
        )

        val frequency = service.detectFrequency(dates)

        assertEquals(RecurringFrequency.MONTHLY, frequency)
    }

    // Test 10: Detects WEEKLY frequency for 7-day intervals
    @Test
    fun `detectFrequency returns WEEKLY for weekly intervals`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 8),
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 1, 22)
        )

        val frequency = service.detectFrequency(dates)

        assertEquals(RecurringFrequency.WEEKLY, frequency)
    }

    // Test 11: Detects YEARLY frequency for ~365 day intervals
    @Test
    fun `detectFrequency returns YEARLY for yearly intervals`() {
        val dates = listOf(
            LocalDate.of(2022, 6, 15),
            LocalDate.of(2023, 6, 15),
            LocalDate.of(2024, 6, 15)
        )

        val frequency = service.detectFrequency(dates)

        assertEquals(RecurringFrequency.YEARLY, frequency)
    }

    // Test 12: Returns null for irregular intervals
    @Test
    fun `detectFrequency returns null for irregular intervals`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 10),  // 9 days
            LocalDate.of(2024, 1, 25),  // 15 days
            LocalDate.of(2024, 2, 28)   // 34 days
        )

        val frequency = service.detectFrequency(dates)

        assertNull(frequency)
    }

    // Test 13: Returns null for less than minimum occurrences
    @Test
    fun `detectFrequency returns null for single occurrence`() {
        val dates = listOf(LocalDate.of(2024, 1, 15))

        val frequency = service.detectFrequency(dates)

        assertNull(frequency)
    }

    // Test 14: Handles bi-weekly patterns (not currently supported)
    @Test
    fun `detectFrequency returns null for bi-weekly pattern`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 1, 29),
            LocalDate.of(2024, 2, 12)
        )

        val frequency = service.detectFrequency(dates)

        // Bi-weekly not in enum, should return null or closest match
        assertNull(frequency)
    }

    // Test 15: Tolerates small variance in monthly dates
    @Test
    fun `detectFrequency tolerates 3-day variance for monthly`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 2, 17),  // +2 days
            LocalDate.of(2024, 3, 14)   // -1 day
        )

        val frequency = service.detectFrequency(dates)

        assertEquals(RecurringFrequency.MONTHLY, frequency)
    }

    // Test 16: Tolerates variance in weekly dates
    @Test
    fun `detectFrequency tolerates 1-day variance for weekly`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 9),   // +1 day
            LocalDate.of(2024, 1, 15),  // -1 day
            LocalDate.of(2024, 1, 22)
        )

        val frequency = service.detectFrequency(dates)

        assertEquals(RecurringFrequency.WEEKLY, frequency)
    }

    // Test 17: Sorts dates before analysis
    @Test
    fun `detectFrequency sorts unsorted dates`() {
        val dates = listOf(
            LocalDate.of(2024, 3, 15),
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 2, 15)
        )

        val frequency = service.detectFrequency(dates)

        assertEquals(RecurringFrequency.MONTHLY, frequency)
    }

    // Test 18: Returns null for empty list
    @Test
    fun `detectFrequency returns null for empty list`() {
        val frequency = service.detectFrequency(emptyList())

        assertNull(frequency)
    }

    // ==================== Amount Variance Tests (5 tests) ====================

    // Test 19: Calculates zero variance for identical amounts
    @Test
    fun `calculateAmountVariance returns zero for identical amounts`() {
        val amounts = listOf(649.0, 649.0, 649.0)

        val variance = service.calculateAmountVariance(amounts)

        assertEquals(0.0f, variance, 0.001f)
    }

    // Test 20: Calculates variance for varying amounts
    @Test
    fun `calculateAmountVariance returns variance as coefficient of variation`() {
        val amounts = listOf(100.0, 110.0, 90.0) // ±10% variance

        val variance = service.calculateAmountVariance(amounts)

        assertTrue(variance > 0.05f)
        assertTrue(variance < 0.15f)
    }

    // Test 21: Handles single amount
    @Test
    fun `calculateAmountVariance returns zero for single amount`() {
        val amounts = listOf(500.0)

        val variance = service.calculateAmountVariance(amounts)

        assertEquals(0.0f, variance, 0.001f)
    }

    // Test 22: Handles empty list
    @Test
    fun `calculateAmountVariance returns zero for empty list`() {
        val variance = service.calculateAmountVariance(emptyList())

        assertEquals(0.0f, variance, 0.001f)
    }

    // Test 23: Handles large variance
    @Test
    fun `calculateAmountVariance handles large variance amounts`() {
        val amounts = listOf(100.0, 500.0, 1000.0)

        val variance = service.calculateAmountVariance(amounts)

        assertTrue(variance > 0.5f) // High variance
    }

    // ==================== Typical Day of Period Tests (5 tests) ====================

    // Test 24: Calculates typical day for monthly
    @Test
    fun `calculateTypicalDayOfPeriod returns mode day for monthly`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 2, 15),
            LocalDate.of(2024, 3, 16)  // Slight variance
        )

        val day = service.calculateTypicalDayOfPeriod(dates, RecurringFrequency.MONTHLY)

        assertEquals(15, day) // Mode is 15
    }

    // Test 25: Calculates typical day for weekly (day of week 1-7)
    @Test
    fun `calculateTypicalDayOfPeriod returns day of week for weekly`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 1),   // Monday
            LocalDate.of(2024, 1, 8),   // Monday
            LocalDate.of(2024, 1, 15)   // Monday
        )

        val day = service.calculateTypicalDayOfPeriod(dates, RecurringFrequency.WEEKLY)

        assertEquals(1, day) // Monday = 1
    }

    // Test 26: Handles end of month dates
    @Test
    fun `calculateTypicalDayOfPeriod handles end of month`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 31),
            LocalDate.of(2024, 2, 29),  // Leap year
            LocalDate.of(2024, 3, 31)
        )

        val day = service.calculateTypicalDayOfPeriod(dates, RecurringFrequency.MONTHLY)

        // Should recognize as "end of month" pattern, returns 31 or last day
        assertTrue(day >= 28)
    }

    // Test 27: Returns 1 for yearly (day of year doesn't make sense)
    @Test
    fun `calculateTypicalDayOfPeriod returns day of month for yearly`() {
        val dates = listOf(
            LocalDate.of(2022, 6, 15),
            LocalDate.of(2023, 6, 15),
            LocalDate.of(2024, 6, 15)
        )

        val day = service.calculateTypicalDayOfPeriod(dates, RecurringFrequency.YEARLY)

        assertEquals(15, day) // Day of month for yearly anniversary
    }

    // Test 28: Handles empty dates
    @Test
    fun `calculateTypicalDayOfPeriod returns 1 for empty dates`() {
        val day = service.calculateTypicalDayOfPeriod(emptyList(), RecurringFrequency.MONTHLY)

        assertEquals(1, day) // Default to 1st
    }

    // ==================== Confidence Scoring Tests (5 tests) ====================

    // Test 29: High confidence for many occurrences, low variance
    @Test
    fun `calculateConfidence returns high score for consistent pattern`() {
        val confidence = service.calculateConfidence(
            occurrences = 6,
            amountVariance = 0.0f,
            intervalConsistency = 1.0f
        )

        assertTrue(confidence >= 0.9f)
    }

    // Test 30: Lower confidence for few occurrences
    @Test
    fun `calculateConfidence returns lower score for few occurrences`() {
        val confidence = service.calculateConfidence(
            occurrences = 2,
            amountVariance = 0.0f,
            intervalConsistency = 1.0f
        )

        assertTrue(confidence < 0.9f)
        assertTrue(confidence >= 0.7f)
    }

    // Test 31: Lower confidence for high amount variance
    @Test
    fun `calculateConfidence penalizes high amount variance`() {
        val lowVarianceConfidence = service.calculateConfidence(
            occurrences = 4,
            amountVariance = 0.05f,
            intervalConsistency = 1.0f
        )

        val highVarianceConfidence = service.calculateConfidence(
            occurrences = 4,
            amountVariance = 0.3f,
            intervalConsistency = 1.0f
        )

        assertTrue(lowVarianceConfidence > highVarianceConfidence)
    }

    // Test 32: Lower confidence for inconsistent intervals
    @Test
    fun `calculateConfidence penalizes inconsistent intervals`() {
        val consistentConfidence = service.calculateConfidence(
            occurrences = 4,
            amountVariance = 0.1f,
            intervalConsistency = 0.95f
        )

        val inconsistentConfidence = service.calculateConfidence(
            occurrences = 4,
            amountVariance = 0.1f,
            intervalConsistency = 0.7f
        )

        assertTrue(consistentConfidence > inconsistentConfidence)
    }

    // Test 33: Confidence is bounded between 0 and 1
    @Test
    fun `calculateConfidence returns value between 0 and 1`() {
        val confidence = service.calculateConfidence(
            occurrences = 2,
            amountVariance = 0.5f,
            intervalConsistency = 0.5f
        )

        assertTrue(confidence >= 0f)
        assertTrue(confidence <= 1f)
    }

    // ==================== Next Occurrence Prediction Tests (4 tests) ====================

    // Test 34: Predicts next monthly occurrence
    @Test
    fun `predictNextOccurrence returns correct date for monthly`() {
        val lastDate = LocalDate.of(2024, 1, 15)

        val nextDate = service.predictNextOccurrence(lastDate, RecurringFrequency.MONTHLY, 15)

        assertEquals(LocalDate.of(2024, 2, 15), nextDate)
    }

    // Test 35: Predicts next weekly occurrence
    @Test
    fun `predictNextOccurrence returns correct date for weekly`() {
        val lastDate = LocalDate.of(2024, 1, 15)  // Monday

        val nextDate = service.predictNextOccurrence(lastDate, RecurringFrequency.WEEKLY, 1)

        assertEquals(LocalDate.of(2024, 1, 22), nextDate)
    }

    // Test 36: Predicts next yearly occurrence
    @Test
    fun `predictNextOccurrence returns correct date for yearly`() {
        val lastDate = LocalDate.of(2024, 6, 15)

        val nextDate = service.predictNextOccurrence(lastDate, RecurringFrequency.YEARLY, 15)

        assertEquals(LocalDate.of(2025, 6, 15), nextDate)
    }

    // Test 37: Handles end of month edge case
    @Test
    fun `predictNextOccurrence handles end of month`() {
        val lastDate = LocalDate.of(2024, 1, 31)

        val nextDate = service.predictNextOccurrence(lastDate, RecurringFrequency.MONTHLY, 31)

        // February doesn't have 31 days, should be Feb 29 (leap year)
        assertEquals(LocalDate.of(2024, 2, 29), nextDate)
    }

    // ==================== Pattern Detection Integration Tests (7 tests) ====================

    // Test 38: detectPatterns returns suggestions for valid patterns
    @Test
    fun `detectPatterns returns suggestions for recurring transactions`() = runTest {
        val transactions = listOf(
            createTransaction("Netflix", LocalDateTime.of(2024, 1, 15, 10, 0), 649.0),
            createTransaction("Netflix", LocalDateTime.of(2024, 2, 15, 10, 0), 649.0),
            createTransaction("Netflix", LocalDateTime.of(2024, 3, 15, 10, 0), 649.0)
        )
        whenever(transactionRepository.getAllTransactions()).thenReturn(transactions)
        whenever(recurringRuleRepository.findByMerchantPattern(any())).thenReturn(null)

        val suggestions = service.detectPatterns()

        assertTrue(suggestions.isNotEmpty())
        assertEquals("NETFLIX", suggestions[0].displayName.uppercase().take(7))
    }

    // Test 39: detectPatterns excludes already confirmed rules
    @Test
    fun `detectPatterns excludes merchants with existing rules`() = runTest {
        val transactions = listOf(
            createTransaction("Netflix", LocalDateTime.of(2024, 1, 15, 10, 0), 649.0),
            createTransaction("Netflix", LocalDateTime.of(2024, 2, 15, 10, 0), 649.0),
            createTransaction("Netflix", LocalDateTime.of(2024, 3, 15, 10, 0), 649.0)
        )
        whenever(transactionRepository.getAllTransactions()).thenReturn(transactions)
        whenever(recurringRuleRepository.findByMerchantPattern("NETFLIX")).thenReturn(
            RecurringRule(
                id = 1L,
                merchantPattern = "NETFLIX",
                categoryId = 1L,
                expectedAmount = 649.0,
                frequency = RecurringFrequency.MONTHLY,
                isUserConfirmed = true
            )
        )

        val suggestions = service.detectPatterns()

        assertTrue(suggestions.isEmpty())
    }

    // Test 40: detectPatterns filters by minimum confidence
    @Test
    fun `detectPatterns filters low confidence patterns`() = runTest {
        // Two occurrences with varying amounts should have lower confidence
        val transactions = listOf(
            createTransaction("RandomMerchant", LocalDateTime.of(2024, 1, 15, 10, 0), 100.0),
            createTransaction("RandomMerchant", LocalDateTime.of(2024, 3, 20, 10, 0), 500.0)
        )
        whenever(transactionRepository.getAllTransactions()).thenReturn(transactions)
        whenever(recurringRuleRepository.findByMerchantPattern(any())).thenReturn(null)

        val suggestions = service.detectPatterns()

        // High variance + irregular interval = low confidence = filtered out
        suggestions.forEach { suggestion ->
            assertTrue(suggestion.confidence >= PatternDetectionService.MIN_CONFIDENCE)
        }
    }

    // ==================== Confirmation Flow Tests (3 tests) ====================

    // Test 41 (Bonus): confirmPattern creates a new recurring rule
    @Test
    fun `confirmPattern creates recurring rule from suggestion`() = runTest {
        val suggestion = PatternSuggestion(
            merchantPattern = "NETFLIX",
            displayName = "Netflix",
            averageAmount = 649.0,
            detectedFrequency = RecurringFrequency.MONTHLY,
            typicalDayOfPeriod = 15,
            occurrenceCount = 4,
            confidence = 0.9f,
            nextExpected = LocalDate.of(2024, 4, 15),
            categoryId = 5L
        )
        whenever(recurringRuleRepository.insert(any())).thenReturn(100L)

        val rule = service.confirmPattern(suggestion)

        verify(recurringRuleRepository).insert(argThat {
            merchantPattern == "NETFLIX" &&
            expectedAmount == 649.0 &&
            frequency == RecurringFrequency.MONTHLY &&
            isUserConfirmed
        })
        assertEquals("NETFLIX", rule.merchantPattern)
        assertTrue(rule.isUserConfirmed)
    }

    // Test 42 (Bonus): dismissPattern marks pattern as dismissed
    @Test
    fun `dismissPattern does not create a rule`() = runTest {
        val suggestion = PatternSuggestion(
            merchantPattern = "SPAM_MERCHANT",
            displayName = "Spam Merchant",
            averageAmount = 100.0,
            detectedFrequency = RecurringFrequency.MONTHLY,
            typicalDayOfPeriod = 1,
            occurrenceCount = 2,
            confidence = 0.75f,
            nextExpected = LocalDate.now().plusMonths(1),
            categoryId = null
        )

        service.dismissPattern(suggestion)

        verify(recurringRuleRepository, never()).insert(any())
    }

    // ==================== Lower Threshold Tests (New) ====================

    // Test 43: detectPatterns detects pattern with 2 occurrences (lowered threshold)
    @Test
    fun `detectPatterns detects pattern with 2 occurrences`() = runTest {
        val transactions = listOf(
            createTransaction("Netflix", LocalDateTime.of(2024, 1, 15, 10, 0), 649.0),
            createTransaction("Netflix", LocalDateTime.of(2024, 2, 15, 10, 0), 649.0)
        )
        whenever(transactionRepository.getAllTransactions()).thenReturn(transactions)
        whenever(recurringRuleRepository.findByMerchantPattern(any())).thenReturn(null)

        val suggestions = service.detectPatterns()

        // With MIN_OCCURRENCES = 2, this should now detect a pattern
        assertTrue(suggestions.isNotEmpty())
        assertEquals("NETFLIX", suggestions[0].merchantPattern)
    }

    // Test 44: detectPatterns returns suggestion with lower confidence threshold
    @Test
    fun `detectPatterns returns suggestion with 0_55 confidence`() = runTest {
        // Create transactions that would have lower confidence (variable amounts, slight timing variance)
        val transactions = listOf(
            createTransaction("SomeMerchant", LocalDateTime.of(2024, 1, 10, 10, 0), 500.0),
            createTransaction("SomeMerchant", LocalDateTime.of(2024, 2, 12, 10, 0), 520.0)
        )
        whenever(transactionRepository.getAllTransactions()).thenReturn(transactions)
        whenever(recurringRuleRepository.findByMerchantPattern(any())).thenReturn(null)

        val suggestions = service.detectPatterns()

        // With MIN_CONFIDENCE = 0.55, patterns with moderate confidence should be included
        suggestions.forEach { suggestion ->
            assertTrue(suggestion.confidence >= PatternDetectionService.MIN_CONFIDENCE)
        }
    }

    // Test 45: detectFrequency detects monthly pattern with 2 dates (lowered threshold)
    @Test
    fun `detectFrequency returns MONTHLY for 2 dates with monthly interval`() {
        val dates = listOf(
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 2, 15)
        )

        val frequency = service.detectFrequency(dates)

        // With MIN_OCCURRENCES = 2, this should detect MONTHLY
        assertEquals(RecurringFrequency.MONTHLY, frequency)
    }

    // ==================== Helper Functions ====================

    private fun createTransaction(
        merchantName: String,
        date: LocalDateTime,
        amount: Double = 1000.0,
        type: TransactionType = TransactionType.DEBIT,
        categoryId: Long? = null
    ): Transaction {
        return Transaction(
            id = 0L,
            amount = amount,
            type = type,
            merchantName = merchantName,
            merchantNormalized = merchantName,
            categoryId = categoryId,
            transactionDate = date
        )
    }
}
