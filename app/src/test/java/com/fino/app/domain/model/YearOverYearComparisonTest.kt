package com.fino.app.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.Month

class YearOverYearComparisonTest {

    private val januaryData = listOf(
        YearlySpendingData(
            year = 2024,
            month = Month.JANUARY,
            totalSpent = 15000.0,
            totalIncome = 50000.0,
            transactionCount = 25
        ),
        YearlySpendingData(
            year = 2023,
            month = Month.JANUARY,
            totalSpent = 12000.0,
            totalIncome = 45000.0,
            transactionCount = 20
        ),
        YearlySpendingData(
            year = 2022,
            month = Month.JANUARY,
            totalSpent = 10000.0,
            totalIncome = 40000.0,
            transactionCount = 18
        )
    )

    @Test
    fun `calculateYearOverYearComparison sorts data by year descending`() {
        val comparison = calculateYearOverYearComparison(Month.JANUARY, januaryData)

        assertEquals(3, comparison.yearlyData.size)
        assertEquals(2024, comparison.yearlyData[0].year)
        assertEquals(2023, comparison.yearlyData[1].year)
        assertEquals(2022, comparison.yearlyData[2].year)
    }

    @Test
    fun `calculateYearOverYearComparison calculates average spending correctly`() {
        val comparison = calculateYearOverYearComparison(Month.JANUARY, januaryData)

        // (15000 + 12000 + 10000) / 3 = 12333.33
        assertEquals(12333.33, comparison.averageSpending, 0.01)
    }

    @Test
    fun `calculateYearOverYearComparison identifies highest spending year`() {
        val comparison = calculateYearOverYearComparison(Month.JANUARY, januaryData)

        assertNotNull(comparison.highestYear)
        assertEquals(2024, comparison.highestYear!!.year)
        assertEquals(15000.0, comparison.highestYear!!.totalSpent, 0.01)
    }

    @Test
    fun `calculateYearOverYearComparison identifies lowest spending year`() {
        val comparison = calculateYearOverYearComparison(Month.JANUARY, januaryData)

        assertNotNull(comparison.lowestYear)
        assertEquals(2022, comparison.lowestYear!!.year)
        assertEquals(10000.0, comparison.lowestYear!!.totalSpent, 0.01)
    }

    @Test
    fun `calculateYearOverYearComparison creates year-over-year changes`() {
        val comparison = calculateYearOverYearComparison(Month.JANUARY, januaryData)

        assertEquals(2, comparison.yearOverYearChanges.size)

        // 2024 vs 2023
        val change2024vs2023 = comparison.yearOverYearChanges[0]
        assertEquals(2023, change2024vs2023.fromYear)
        assertEquals(2024, change2024vs2023.toYear)
        assertEquals(3000.0, change2024vs2023.spendingChange, 0.01)
        // (15000 - 12000) / 12000 * 100 = 25%
        assertEquals(25.0f, change2024vs2023.spendingChangePercentage, 0.1f)

        // 2023 vs 2022
        val change2023vs2022 = comparison.yearOverYearChanges[1]
        assertEquals(2022, change2023vs2022.fromYear)
        assertEquals(2023, change2023vs2022.toYear)
        assertEquals(2000.0, change2023vs2022.spendingChange, 0.01)
        // (12000 - 10000) / 10000 * 100 = 20%
        assertEquals(20.0f, change2023vs2022.spendingChangePercentage, 0.1f)
    }

    @Test
    fun `calculateYearOverYearComparison calculates income changes`() {
        val comparison = calculateYearOverYearComparison(Month.JANUARY, januaryData)

        val change2024vs2023 = comparison.yearOverYearChanges[0]
        assertEquals(5000.0, change2024vs2023.incomeChange, 0.01)
        // (50000 - 45000) / 45000 * 100 = 11.11%
        assertEquals(11.11f, change2024vs2023.incomeChangePercentage, 0.1f)
    }

    @Test
    fun `calculateYearOverYearComparison assigns correct trend direction`() {
        val comparison = calculateYearOverYearComparison(Month.JANUARY, januaryData)

        // Both changes are positive (increasing spending), should be INCREASING
        assertEquals(TrendDirection.INCREASING, comparison.yearOverYearChanges[0].trend)
        assertEquals(TrendDirection.INCREASING, comparison.yearOverYearChanges[1].trend)
    }

    @Test
    fun `calculateYearOverYearComparison handles single year data`() {
        val singleYearData = listOf(januaryData[0])
        val comparison = calculateYearOverYearComparison(Month.JANUARY, singleYearData)

        assertEquals(1, comparison.yearlyData.size)
        assertEquals(15000.0, comparison.averageSpending, 0.01)
        assertEquals(0, comparison.yearOverYearChanges.size)
        assertEquals(2024, comparison.highestYear!!.year)
        assertEquals(2024, comparison.lowestYear!!.year)
    }

    @Test
    fun `calculateYearOverYearComparison handles empty data`() {
        val comparison = calculateYearOverYearComparison(Month.JANUARY, emptyList())

        assertEquals(0, comparison.yearlyData.size)
        assertEquals(0.0, comparison.averageSpending, 0.01)
        assertEquals(0, comparison.yearOverYearChanges.size)
        assertNull(comparison.highestYear)
        assertNull(comparison.lowestYear)
    }

    @Test
    fun `calculateYearOverYearComparison handles decreasing spending trend`() {
        val decreasingData = listOf(
            YearlySpendingData(2024, Month.FEBRUARY, 8000.0, 50000.0, 15),
            YearlySpendingData(2023, Month.FEBRUARY, 12000.0, 48000.0, 20)
        )

        val comparison = calculateYearOverYearComparison(Month.FEBRUARY, decreasingData)

        val change = comparison.yearOverYearChanges[0]
        assertEquals(-4000.0, change.spendingChange, 0.01)
        // (8000 - 12000) / 12000 * 100 = -33.33%
        assertEquals(-33.33f, change.spendingChangePercentage, 0.1f)
        assertEquals(TrendDirection.DECREASING, change.trend)
    }

    @Test
    fun `YearlySpendingData stores all required fields correctly`() {
        val data = januaryData[0]
        assertEquals(2024, data.year)
        assertEquals(Month.JANUARY, data.month)
        assertEquals(15000.0, data.totalSpent, 0.01)
        assertEquals(50000.0, data.totalIncome, 0.01)
        assertEquals(25, data.transactionCount)
    }

    @Test
    fun `month name is formatted correctly`() {
        val comparison = calculateYearOverYearComparison(Month.JANUARY, januaryData)
        assertEquals("January", comparison.monthName)

        val marchComparison = calculateYearOverYearComparison(Month.MARCH, emptyList())
        assertEquals("March", marchComparison.monthName)
    }
}
