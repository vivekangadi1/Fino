package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.TransactionRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType
import com.fino.app.domain.model.UserStats
import com.fino.app.gamification.LevelCalculator
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
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockUserStatsRepository: UserStatsRepository
    private lateinit var levelCalculator: LevelCalculator
    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testTransactions = listOf(
        Transaction(
            id = 1L,
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            transactionDate = LocalDateTime.now().minusDays(1),
            source = TransactionSource.MANUAL
        ),
        Transaction(
            id = 2L,
            amount = 200.0,
            type = TransactionType.DEBIT,
            merchantName = "Uber",
            transactionDate = LocalDateTime.now().minusDays(2),
            source = TransactionSource.SMS
        ),
        Transaction(
            id = 3L,
            amount = 50000.0,
            type = TransactionType.CREDIT,
            merchantName = "Salary",
            transactionDate = LocalDateTime.now().minusDays(5),
            source = TransactionSource.MANUAL
        )
    )

    private val testUserStats = UserStats(
        id = 1L,
        currentStreak = 5,
        longestStreak = 10,
        totalTransactionsLogged = 50,
        totalXp = 350,
        currentLevel = 3
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockTransactionRepository = mock()
        mockUserStatsRepository = mock()
        levelCalculator = LevelCalculator()

        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(testTransactions))
        whenever(mockUserStatsRepository.getUserStatsFlow()).thenReturn(flowOf(testUserStats))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            transactionRepository = mockTransactionRepository,
            userStatsRepository = mockUserStatsRepository,
            levelCalculator = levelCalculator
        )
    }

    // Test 1: Initial state is loading
    @Test
    fun `initial state shows loading`() = runTest {
        viewModel = createViewModel()
        val initialState = viewModel.uiState.value

        assertTrue(initialState.isLoading)
    }

    // Test 2: calculates totalBalance correctly
    @Test
    fun `calculates total balance from all transactions`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Income (50000) - Expenses (500 + 200) = 49300
        assertEquals(49300.0, state.totalBalance, 0.01)
    }

    // Test 3: calculates monthly spent from DEBIT transactions
    @Test
    fun `calculates monthly spent from DEBIT transactions`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // 500 + 200 = 700
        assertEquals(700.0, state.monthlySpent, 0.01)
    }

    // Test 4: calculates monthly income from CREDIT transactions
    @Test
    fun `calculates monthly income from CREDIT transactions`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(50000.0, state.monthlyIncome, 0.01)
    }

    // Test 5: fetches recent transactions limited to 5
    @Test
    fun `fetches recent transactions limited to 5`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.recentTransactions.size <= 5)
        assertEquals(3, state.recentTransactions.size)
    }

    // Test 6: recent transactions are sorted by date (newest first)
    @Test
    fun `recent transactions are sorted by date descending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Check transactions are sorted by date descending
        for (i in 0 until state.recentTransactions.size - 1) {
            assertTrue(
                state.recentTransactions[i].transactionDate >=
                state.recentTransactions[i + 1].transactionDate
            )
        }
    }

    // Test 7: fetches currentStreak from userStats
    @Test
    fun `fetches current streak from user stats`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(5, state.currentStreak)
    }

    // Test 8: fetches currentLevel from userStats
    @Test
    fun `fetches current level from user stats`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(3, state.currentLevel)
    }

    // Test 9: calculates xpProgress correctly
    @Test
    fun `calculates xp progress correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // XP progress should be between 0 and 1
        assertTrue(state.xpProgress in 0f..1f)
    }

    // Test 10: handles empty transactions list
    @Test
    fun `handles empty transactions list`() = runTest {
        whenever(mockTransactionRepository.getAllTransactionsFlow()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(0.0, state.totalBalance, 0.01)
        assertEquals(0.0, state.monthlySpent, 0.01)
        assertEquals(0.0, state.monthlyIncome, 0.01)
        assertTrue(state.recentTransactions.isEmpty())
    }

    // Test 11: handles null userStats
    @Test
    fun `handles null user stats with defaults`() = runTest {
        whenever(mockUserStatsRepository.getUserStatsFlow()).thenReturn(flowOf(null))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(0, state.currentStreak)
        assertEquals(1, state.currentLevel)
    }

    // Test 12: isLoading becomes false after data loads
    @Test
    fun `isLoading becomes false after data loads`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
    }
}
