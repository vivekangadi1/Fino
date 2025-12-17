package com.fino.app.ml

import com.fino.app.data.repository.MerchantMappingRepository
import com.fino.app.domain.model.MatchType
import com.fino.app.domain.model.MerchantMapping
import com.fino.app.ml.matcher.MerchantMatcher
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime

class MerchantMatcherTest {

    private lateinit var matcher: MerchantMatcher
    private lateinit var mockRepository: MerchantMappingRepository

    @Before
    fun setup() {
        mockRepository = mock()
        matcher = MerchantMatcher(mockRepository)
    }

    @Test
    fun `exact match - returns mapping with high confidence`() = runBlocking {
        val mapping = MerchantMapping(
            id = 1,
            rawMerchantName = "MY CHICKEN SHOP",
            normalizedName = "My Chicken Shop",
            categoryId = 3,
            confidence = 0.95f,
            matchCount = 10,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )
        whenever(mockRepository.findByRawName("MY CHICKEN SHOP")).thenReturn(mapping)

        val result = matcher.findMatch("MY CHICKEN SHOP")

        assertEquals(MatchType.EXACT, result.matchType)
        assertEquals(mapping, result.mapping)
        assertTrue(result.confidence >= 0.95f)
        assertFalse(result.requiresConfirmation)
    }

    @Test
    fun `fuzzy match - returns mapping with medium confidence and requires confirmation`() = runBlocking {
        val mapping = MerchantMapping(
            id = 1,
            rawMerchantName = "AMAZON PRIME VIDEO",
            normalizedName = "Amazon Prime",
            categoryId = 3,
            confidence = 0.9f,
            matchCount = 10,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )
        // Very similar - only "VIDEO" vs "MUSIC"
        whenever(mockRepository.findByRawName("AMAZON PRIME MUSIC")).thenReturn(null)
        whenever(mockRepository.findAllMappings()).thenReturn(listOf(mapping))

        val result = matcher.findMatch("AMAZON PRIME MUSIC")

        assertEquals(MatchType.FUZZY, result.matchType)
        assertTrue(result.confidence >= 0.7f)
        assertTrue(result.requiresConfirmation)
    }

    @Test
    fun `no match - returns null mapping`() = runBlocking {
        whenever(mockRepository.findByRawName("TOTALLY NEW MERCHANT")).thenReturn(null)
        whenever(mockRepository.findAllMappings()).thenReturn(emptyList())

        val result = matcher.findMatch("TOTALLY NEW MERCHANT")

        assertEquals(MatchType.NONE, result.matchType)
        assertNull(result.mapping)
    }

    @Test
    fun `calculate similarity - identical strings return 1`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "MY CHICKEN SHOP")

        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `calculate similarity - similar strings return high score`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "MY CHICKEN STORE")

        assertTrue(score >= 0.7f)
    }

    @Test
    fun `calculate similarity - different strings return low score`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "AMAZON PRIME")

        assertTrue(score < 0.3f)
    }

    @Test
    fun `calculate similarity - case insensitive`() {
        val score = matcher.calculateSimilarity("my chicken shop", "MY CHICKEN SHOP")

        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `calculate similarity - handles extra spaces`() {
        val score = matcher.calculateSimilarity("MY  CHICKEN   SHOP", "MY CHICKEN SHOP")

        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `fuzzy match - identifies similar merchants across branches`() = runBlocking {
        val mapping = MerchantMapping(
            rawMerchantName = "SWIGGY ORDER 12345",
            normalizedName = "Swiggy",
            categoryId = 5,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )
        // Similar - branch location differences
        whenever(mockRepository.findByRawName("SWIGGY ORDER 54321")).thenReturn(null)
        whenever(mockRepository.findAllMappings()).thenReturn(listOf(mapping))

        val result = matcher.findMatch("SWIGGY ORDER 54321")

        assertEquals(MatchType.FUZZY, result.matchType)
        assertTrue(result.confidence >= 0.7f)
    }

    @Test
    fun `confirm fuzzy match - creates new mapping`() = runBlocking {
        val existingMapping = MerchantMapping(
            id = 1,
            rawMerchantName = "MY CHICKEN SHOP",
            normalizedName = "My Chicken Shop",
            categoryId = 3,
            confidence = 0.8f,
            matchCount = 5,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )

        matcher.confirmFuzzyMatch("CHICKEN CORNER", existingMapping)

        verify(mockRepository).insertMapping(argThat {
            rawMerchantName == "CHICKEN CORNER" &&
            categoryId == existingMapping.categoryId &&
            isFuzzyMatch == true
        })
        Unit
    }
}
