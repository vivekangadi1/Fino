package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.data.repository.UserStatsRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType
import com.fino.app.gamification.StreakTracker
import com.fino.app.gamification.XpAction
import com.fino.app.gamification.XpCalculator
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockCategoryRepository: CategoryRepository
    private lateinit var mockUserStatsRepository: UserStatsRepository
    private lateinit var mockStreakTracker: StreakTracker
    private lateinit var xpCalculator: XpCalculator
    private lateinit var viewModel: AddTransactionViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testCategories = listOf(
        Category(id = 1L, name = "Food", emoji = "🍔"),
        Category(id = 2L, name = "Transport", emoji = "🚗"),
        Category(id = 3L, name = "Shopping", emoji = "🛍️")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockTransactionRepository = mock()
        mockCategoryRepository = mock()
        mockUserStatsRepository = mock()
        mockStreakTracker = mock()
        xpCalculator = XpCalculator()

        whenever(mockCategoryRepository.getAllActive()).thenReturn(flowOf(testCategories))

        viewModel = AddTransactionViewModel(
            transactionRepository = mockTransactionRepository,
            categoryRepository = mockCategoryRepository,
            userStatsRepository = mockUserStatsRepository,
            streakTracker = mockStreakTracker,
            xpCalculator = xpCalculator
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test 1: Initial state is correct
    @Test
    fun `initial state has default values`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals("", state.amount)
        assertEquals("", state.merchant)
        assertNull(state.selectedCategoryId)
        assertEquals(TransactionType.DEBIT, state.transactionType)
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertNull(state.error)
    }

    // Test 2: Categories are loaded on init
    @Test
    fun `categories are loaded from repository on init`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(3, state.categories.size)
        assertEquals("Food", state.categories[0].name)
    }

    // Test 3: setAmount updates state
    @Test
    fun `setAmount updates amount in state`() = runTest {
        viewModel.setAmount("150.50")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals("150.50", state.amount)
    }

    // Test 4: setMerchant updates state
    @Test
    fun `setMerchant updates merchant in state`() = runTest {
        viewModel.setMerchant("Swiggy")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals("Swiggy", state.merchant)
    }

    // Test 5: selectCategory updates state
    @Test
    fun `selectCategory updates selectedCategoryId in state`() = runTest {
        viewModel.selectCategory(2L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(2L, state.selectedCategoryId)
    }

    // Test 6: setTransactionType updates state
    @Test
    fun `setTransactionType updates transactionType in state`() = runTest {
        viewModel.setTransactionType(TransactionType.CREDIT)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(TransactionType.CREDIT, state.transactionType)
    }

    // Test 7: saveTransaction fails when amount is empty
    @Test
    fun `saveTransaction sets error when amount is empty`() = runTest {
        viewModel.selectCategory(1L)
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertNotNull(state.error)
        assertTrue(state.error!!.contains("amount"))
        assertFalse(state.saveSuccess)
    }

    // Test 8: saveTransaction fails when category not selected
    @Test
    fun `saveTransaction sets error when category not selected`() = runTest {
        viewModel.setAmount("100")
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertNotNull(state.error)
        assertTrue(state.error!!.contains("category"))
        assertFalse(state.saveSuccess)
    }

    // Test 9: saveTransaction creates DEBIT transaction for expense
    @Test
    fun `saveTransaction creates DEBIT transaction when transactionType is DEBIT`() = runTest {
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)
        whenever(mockStreakTracker.recordActivity(any())).thenReturn(1)

        viewModel.setAmount("200")
        viewModel.setMerchant("Amazon")
        viewModel.selectCategory(3L)
        viewModel.setTransactionType(TransactionType.DEBIT)
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockTransactionRepository).insert(argThat { transaction ->
            transaction.type == TransactionType.DEBIT &&
            transaction.amount == 200.0 &&
            transaction.merchantName == "Amazon" &&
            transaction.categoryId == 3L
        })
    }

    // Test 10: saveTransaction creates CREDIT transaction for income
    @Test
    fun `saveTransaction creates CREDIT transaction when transactionType is CREDIT`() = runTest {
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)
        whenever(mockStreakTracker.recordActivity(any())).thenReturn(1)

        viewModel.setAmount("50000")
        viewModel.setMerchant("Salary")
        viewModel.selectCategory(1L)
        viewModel.setTransactionType(TransactionType.CREDIT)
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockTransactionRepository).insert(argThat { transaction ->
            transaction.type == TransactionType.CREDIT &&
            transaction.amount == 50000.0
        })
    }

    // Test 10b: saveTransaction creates SAVINGS transaction
    @Test
    fun `saveTransaction creates SAVINGS transaction when transactionType is SAVINGS`() = runTest {
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)
        whenever(mockStreakTracker.recordActivity(any())).thenReturn(1)

        viewModel.setAmount("10000")
        viewModel.setMerchant("SIP Investment")
        viewModel.selectCategory(1L)
        viewModel.setTransactionType(TransactionType.SAVINGS)
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockTransactionRepository).insert(argThat { transaction ->
            transaction.type == TransactionType.SAVINGS &&
            transaction.amount == 10000.0
        })
    }

    // Test 11: saveTransaction sets source to MANUAL
    @Test
    fun `saveTransaction sets source to MANUAL`() = runTest {
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)
        whenever(mockStreakTracker.recordActivity(any())).thenReturn(1)

        viewModel.setAmount("100")
        viewModel.selectCategory(1L)
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockTransactionRepository).insert(argThat { transaction ->
            transaction.source == TransactionSource.MANUAL
        })
    }

    // Test 12: saveTransaction increments transaction count
    @Test
    fun `saveTransaction increments transaction count`() = runTest {
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)
        whenever(mockStreakTracker.recordActivity(any())).thenReturn(1)

        viewModel.setAmount("100")
        viewModel.selectCategory(1L)
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockUserStatsRepository).incrementTransactionCount()
    }

    // Test 13: saveTransaction records streak activity
    @Test
    fun `saveTransaction records streak activity for today`() = runTest {
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)
        whenever(mockStreakTracker.recordActivity(any())).thenReturn(1)

        viewModel.setAmount("100")
        viewModel.selectCategory(1L)
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockStreakTracker).recordActivity(LocalDate.now())
    }

    // Test 14: saveTransaction adds XP for manual transaction
    @Test
    fun `saveTransaction adds XP for manual transaction`() = runTest {
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)
        whenever(mockStreakTracker.recordActivity(any())).thenReturn(1)

        viewModel.setAmount("100")
        viewModel.selectCategory(1L)
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        val expectedXp = xpCalculator.getXpForAction(XpAction.ADD_MANUAL_TRANSACTION)
        verify(mockUserStatsRepository).addXp(expectedXp)
    }

    // Test 15: saveTransaction sets saveSuccess to true on success
    @Test
    fun `saveTransaction sets saveSuccess to true on success`() = runTest {
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)
        whenever(mockStreakTracker.recordActivity(any())).thenReturn(1)

        viewModel.setAmount("100")
        viewModel.selectCategory(1L)
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state.saveSuccess)
        assertNull(state.error)
    }

    // Test 16: clearError resets error in state
    @Test
    fun `clearError resets error in state`() = runTest {
        // First cause an error
        viewModel.saveTransaction()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error exists
        var state = viewModel.uiState.first()
        assertNotNull(state.error)

        // Clear the error
        viewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.first()
        assertNull(state.error)
    }

    // Test 17: Amount validation - only allows valid numbers
    @Test
    fun `setAmount handles decimal values correctly`() = runTest {
        viewModel.setAmount("1234.56")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals("1234.56", state.amount)
    }
}
