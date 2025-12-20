package com.fino.app.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.YearMonth

class PeriodComparisonTest {

    private val testCategoryBreakdown1 = listOf(
        CategorySpending("Food", 1L, 5000.0, 50f, 10),
        CategorySpending("Transport", 2L, 3000.0, 30f, 8),
        CategorySpending("Entertainment", 3L, 2000.0, 20f, 5)
    )

    private val testCategoryBreakdown2 = listOf(
        CategorySpending("Food", 1L, 6000.0, 54.5f, 12),
        CategorySpending("Transport", 2L, 2500.0, 22.7f, 6),
        CategorySpending("Entertainment", 3L, 2500.0, 22.7f, 7)
    )

    private val currentPeriod = PeriodData(
        yearMonth = YearMonth.of(2024, 2),
        totalIncome = 50000.0,
        totalExpenses = 10000.0,
        netBalance = 40000.0,
        transactionCount = 23,
        categoryBreakdown = testCategoryBreakdown1
    )

    private val previousPeriod = PeriodData(
        yearMonth = YearMonth.of(2024, 1),
        totalIncome = 45000.0,
        totalExpenses = 11000.0,
        netBalance = 34000.0,
        transactionCount = 25,
        categoryBreakdown = testCategoryBreakdown2
    )

    @Test
    fun `calculatePeriodComparison computes income change correctly`() {
        val comparison = calculatePeriodComparison(currentPeriod, previousPeriod)

        assertEquals(5000.0, comparison.incomeChange, 0.01)
        // (50000 - 45000) / 45000 * 100 = 11.11%
        assertEquals(11.11f, comparison.incomeChangePercentage, 0.1f)
    }

    @Test
    fun `calculatePeriodComparison computes expenses change correctly`() {
        val comparison = calculatePeriodComparison(currentPeriod, previousPeriod)

        assertEquals(-1000.0, comparison.expensesChange, 0.01)
        // (10000 - 11000) / 11000 * 100 = -9.09%
        assertEquals(-9.09f, comparison.expensesChangePercentage, 0.1f)
    }

    @Test
    fun `calculatePeriodComparison computes net balance change correctly`() {
        val comparison = calculatePeriodComparison(currentPeriod, previousPeriod)

        assertEquals(6000.0, comparison.netBalanceChange, 0.01)
    }

    @Test
    fun `calculatePeriodComparison creates category comparisons`() {
        val comparison = calculatePeriodComparison(currentPeriod, previousPeriod)

        assertEquals(3, comparison.categoryComparisons.size)

        // Food: 5000 vs 6000 = -1000 (decreased)
        val foodComparison = comparison.categoryComparisons.find { it.categoryName == "Food" }
        assertNotNull(foodComparison)
        assertEquals(-1000.0, foodComparison!!.change, 0.01)
        assertEquals(5000.0, foodComparison.currentAmount, 0.01)
        assertEquals(6000.0, foodComparison.previousAmount, 0.01)
    }

    @Test
    fun `calculatePeriodComparison sorts categories by absolute change`() {
        val comparison = calculatePeriodComparison(currentPeriod, previousPeriod)

        // Food has the largest absolute change (-1000)
        assertEquals("Food", comparison.categoryComparisons[0].categoryName)
    }

    @Test
    fun `calculatePeriodComparison handles new categories`() {
        val currentWithNewCategory = currentPeriod.copy(
            categoryBreakdown = currentPeriod.categoryBreakdown +
                CategorySpending("Shopping", 4L, 1500.0, 15f, 3)
        )

        val comparison = calculatePeriodComparison(currentWithNewCategory, previousPeriod)

        val shoppingComparison = comparison.categoryComparisons.find { it.categoryName == "Shopping" }
        assertNotNull(shoppingComparison)
        assertEquals(1500.0, shoppingComparison!!.currentAmount, 0.01)
        assertEquals(0.0, shoppingComparison.previousAmount, 0.01)
        assertEquals(1500.0, shoppingComparison.change, 0.01)
    }

    @Test
    fun `calculatePeriodComparison handles removed categories`() {
        val currentWithoutEntertainment = currentPeriod.copy(
            categoryBreakdown = currentPeriod.categoryBreakdown.filter { it.categoryName != "Entertainment" }
        )

        val comparison = calculatePeriodComparison(currentWithoutEntertainment, previousPeriod)

        val entertainmentComparison = comparison.categoryComparisons.find { it.categoryName == "Entertainment" }
        assertNotNull(entertainmentComparison)
        assertEquals(0.0, entertainmentComparison!!.currentAmount, 0.01)
        assertEquals(2500.0, entertainmentComparison.previousAmount, 0.01)
        assertEquals(-2500.0, entertainmentComparison.change, 0.01)
    }

    @Test
    fun `calculatePeriodComparison assigns correct trend direction`() {
        val comparison = calculatePeriodComparison(currentPeriod, previousPeriod)

        // Food decreased by -16.67%, should be DECREASING
        val foodComparison = comparison.categoryComparisons.find { it.categoryName == "Food" }
        assertEquals(TrendDirection.DECREASING, foodComparison!!.trend)

        // Transport increased by 20%, should be INCREASING
        val transportComparison = comparison.categoryComparisons.find { it.categoryName == "Transport" }
        assertEquals(TrendDirection.INCREASING, transportComparison!!.trend)
    }

    @Test
    fun `PeriodData stores all required fields correctly`() {
        assertEquals(YearMonth.of(2024, 2), currentPeriod.yearMonth)
        assertEquals(50000.0, currentPeriod.totalIncome, 0.01)
        assertEquals(10000.0, currentPeriod.totalExpenses, 0.01)
        assertEquals(40000.0, currentPeriod.netBalance, 0.01)
        assertEquals(23, currentPeriod.transactionCount)
        assertEquals(3, currentPeriod.categoryBreakdown.size)
    }

    @Test
    fun `CategorySpending stores all required fields correctly`() {
        val category = testCategoryBreakdown1[0]
        assertEquals("Food", category.categoryName)
        assertEquals(1L, category.categoryId)
        assertEquals(5000.0, category.amount, 0.01)
        assertEquals(50f, category.percentage, 0.01f)
        assertEquals(10, category.transactionCount)
    }
}
