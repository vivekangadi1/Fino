package com.fino.app.ml

import com.fino.app.ml.matcher.FuzzyMatcher
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FuzzyMatcherTest {

    private lateinit var matcher: FuzzyMatcher

    @Before
    fun setup() {
        matcher = FuzzyMatcher()
    }

    @Test
    fun `identical strings return similarity 1`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "MY CHICKEN SHOP")

        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `similar strings return high similarity`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "MY CHICKEN STORE")

        assertTrue(score >= 0.7f)
    }

    @Test
    fun `different strings return low similarity`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "AMAZON PRIME")

        assertTrue(score < 0.3f)
    }

    @Test
    fun `empty string returns 0`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "")

        assertEquals(0.0f, score, 0.01f)
    }

    @Test
    fun `case insensitive matching`() {
        val score = matcher.calculateSimilarity("my chicken shop", "MY CHICKEN SHOP")

        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `whitespace normalization`() {
        val score = matcher.calculateSimilarity("MY  CHICKEN   SHOP", "MY CHICKEN SHOP")

        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `find best match returns highest score`() {
        val candidates = listOf(
            "AMAZON PRIME",
            "CHICKEN CORNER",
            "MY CHICKEN STORE",
            "NETFLIX"
        )

        val result = matcher.findBestMatch("MY CHICKEN SHOP", candidates)

        assertNotNull(result)
        assertEquals("MY CHICKEN STORE", result!!.match)
    }

    @Test
    fun `find best match returns null below threshold`() {
        val candidates = listOf(
            "AMAZON PRIME",
            "NETFLIX",
            "SPOTIFY"
        )

        val result = matcher.findBestMatch("MY CHICKEN SHOP", candidates, threshold = 0.7f)

        assertNull(result)
    }
}
