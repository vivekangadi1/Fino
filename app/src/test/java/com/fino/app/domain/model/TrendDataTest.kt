package com.fino.app.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.YearMonth

class TrendDataTest {

    @Test
    fun `calculatePercentageChange returns correct positive change`() {
        val result = calculatePercentageChange(current = 150.0, previous = 100.0)
        assertEquals(50.0f, result, 0.01f)
    }

    @Test
    fun `calculatePercentageChange returns correct negative change`() {
        val result = calculatePercentageChange(current = 75.0, previous = 100.0)
        assertEquals(-25.0f, result, 0.01f)
    }

    @Test
    fun `calculatePercentageChange handles zero previous value`() {
        val result = calculatePercentageChange(current = 100.0, previous = 0.0)
        assertEquals(0.0f, result, 0.01f)
    }

    @Test
    fun `TrendDirection INCREASING when change is greater than 5 percent`() {
        val trend = determineTrendDirection(10.0f)
        assertEquals(TrendDirection.INCREASING, trend)
    }

    @Test
    fun `TrendDirection DECREASING when change is less than minus 5 percent`() {
        val trend = determineTrendDirection(-10.0f)
        assertEquals(TrendDirection.DECREASING, trend)
    }

    @Test
    fun `TrendDirection STABLE when change is between minus 5 and 5 percent`() {
        val trend = determineTrendDirection(3.0f)
        assertEquals(TrendDirection.STABLE, trend)
    }
}
