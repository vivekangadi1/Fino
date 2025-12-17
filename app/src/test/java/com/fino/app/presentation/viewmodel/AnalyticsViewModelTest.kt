package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: CategoryRepository
    private lateinit var viewModel: AnalyticsViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testCategories = listOf(
        Category(id = 1L, name = "Food", emoji = "🍔"),
        Category(id = 2L, name = "Transport", emoji = "🚗"),
        Category(id = 3L, name = "Shopping", emoji = "🛍️")
    )

    private val testTransactions = listOf(
        Transaction(
            id = 1L,
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            categoryId = 1L,
            transactionDate = LocalDateTime.now().minusDays(1),
            source = TransactionSource.MANUAL
        ),
        Transaction(
            id = 2L,
            amount = 300.0,
            type = TransactionType.DEBIT,
            merchantName = "Zomato",
            categoryId = 1L,
            transactionDate = LocalDateTime.now().minusDays(2),
            source = TransactionSource.SMS
        ),
        Transaction(
            id = 3L,
            amount = 200.0,
            type = TransactionType.DEBIT,
            merchantName = "Uber",
            categoryId = 2L,
            transactionDate = LocalDateTime.now().minusDays(3),
            source = TransactionSource.MANUAL
        ),
        Transaction(
            id = 4L,
            amount = 1000.0,
            type = TransactionType.DEBIT,
            merchantName = "Amazon",
            categoryId = 3L,
            transactionDate = LocalDateTime.now().minusDays(4),
            source = TransactionSource.SMS
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockTransactionRepository = mock()
        mockCategoryRepository = mock()

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(testTransactions))
        whenever(mockCategoryRepository.getAllActive()).thenReturn(flowOf(testCategories))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AnalyticsViewModel {
        return AnalyticsViewModel(
            transactionRepository = mockTransactionRepository,
            categoryRepository = mockCategoryRepository
        )
    }

    // Test 1: Initial state shows loading
    @Test
    fun `initial state shows loading`() = runTest {
        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertTrue(state.isLoading)
    }

    // Test 2: Default period is MONTH
    @Test
    fun `default period is MONTH`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(AnalyticsPeriod.MONTH, state.selectedPeriod)
    }

    // Test 3: Calculates total spent correctly
    @Test
    fun `calculates total spent from DEBIT transactions`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // 500 + 300 + 200 + 1000 = 2000
        assertEquals(2000.0, state.totalSpent, 0.01)
    }

    // Test 4: Counts transactions correctly
    @Test
    fun `counts transactions correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(4, state.transactionCount)
    }

    // Test 5: Creates category breakdown
    @Test
    fun `creates category breakdown with spending per category`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.categoryBreakdown.isNotEmpty())
    }

    // Test 6: Category breakdown calculates spending correctly
    @Test
    fun `category breakdown calculates spending correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        val foodCategory = state.categoryBreakdown.find { it.categoryName == "Food" }
        assertNotNull(foodCategory)
        assertEquals(800.0, foodCategory!!.amount, 0.01) // 500 + 300
    }

    // Test 7: Category breakdown calculates percentages
    @Test
    fun `category breakdown calculates percentage of total`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        val foodCategory = state.categoryBreakdown.find { it.categoryName == "Food" }
        assertNotNull(foodCategory)
        // Food is 800 out of 2000 total = 40%
        assertEquals(0.4f, foodCategory!!.percentage, 0.01f)
    }

    // Test 8: Category breakdown is sorted by amount descending
    @Test
    fun `category breakdown is sorted by amount descending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        for (i in 0 until state.categoryBreakdown.size - 1) {
            assertTrue(
                state.categoryBreakdown[i].amount >= state.categoryBreakdown[i + 1].amount
            )
        }
    }

    // Test 9: setPeriod updates selected period
    @Test
    fun `setPeriod updates selected period`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setPeriod(AnalyticsPeriod.WEEK)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(AnalyticsPeriod.WEEK, state.selectedPeriod)
    }

    // Test 10: Handles empty transactions
    @Test
    fun `handles empty transactions list`() = runTest {
        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(0.0, state.totalSpent, 0.01)
        assertEquals(0, state.transactionCount)
        assertTrue(state.categoryBreakdown.isEmpty())
    }
}
