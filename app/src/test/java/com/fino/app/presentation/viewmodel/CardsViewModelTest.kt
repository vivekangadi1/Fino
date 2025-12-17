package com.fino.app.presentation.viewmodel

import com.fino.app.data.repository.CreditCardRepository
import com.fino.app.domain.model.CreditCard
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
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class CardsViewModelTest {

    private lateinit var mockCreditCardRepository: CreditCardRepository
    private lateinit var viewModel: CardsViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testCards = listOf(
        CreditCard(
            id = 1L,
            bankName = "HDFC",
            cardName = "Regalia",
            lastFourDigits = "1234",
            creditLimit = 200000.0,
            billingCycleDay = 15,
            dueDateDay = 5,
            currentUnbilled = 15000.0,
            previousDue = 25000.0,
            previousDueDate = LocalDate.now().plusDays(10),
            minimumDue = 2500.0,
            isActive = true,
            createdAt = LocalDateTime.now().minusMonths(6)
        ),
        CreditCard(
            id = 2L,
            bankName = "ICICI",
            cardName = "Amazon Pay",
            lastFourDigits = "5678",
            creditLimit = 150000.0,
            billingCycleDay = 20,
            dueDateDay = 10,
            currentUnbilled = 8000.0,
            previousDue = 0.0,
            previousDueDate = null,
            minimumDue = null,
            isActive = true,
            createdAt = LocalDateTime.now().minusMonths(3)
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockCreditCardRepository = mock()

        whenever(mockCreditCardRepository.getActiveCardsFlow()).thenReturn(flowOf(testCards))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CardsViewModel {
        return CardsViewModel(creditCardRepository = mockCreditCardRepository)
    }

    // Test 1: Initial state shows loading
    @Test
    fun `initial state shows loading`() = runTest {
        viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertTrue(state.isLoading)
    }

    // Test 2: Fetches cards from repository
    @Test
    fun `fetches cards from repository`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertEquals(2, state.cards.size)
    }

    // Test 3: Cards are loaded correctly
    @Test
    fun `cards contain correct bank names`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.cards.any { it.bankName == "HDFC" })
        assertTrue(state.cards.any { it.bankName == "ICICI" })
    }

    // Test 4: Handles empty cards list
    @Test
    fun `handles empty cards list`() = runTest {
        whenever(mockCreditCardRepository.getActiveCardsFlow()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertTrue(state.cards.isEmpty())
    }

    // Test 5: isLoading becomes false after data loads
    @Test
    fun `isLoading becomes false after data loads`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        assertFalse(state.isLoading)
    }

    // Test 6: Calculates total credit limit
    @Test
    fun `calculates total credit limit from all cards`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // 200000 + 150000 = 350000
        assertEquals(350000.0, state.totalCreditLimit, 0.01)
    }

    // Test 7: Calculates total outstanding
    @Test
    fun `calculates total outstanding from all cards`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()

        // Card 1: unbilled 15000 + due 25000 = 40000
        // Card 2: unbilled 8000 + due 0 = 8000
        // Total = 48000
        assertEquals(48000.0, state.totalOutstanding, 0.01)
    }
}
