package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.RecurringRuleRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.domain.model.RecurringRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddRecurringBillViewModelTest {

    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var recurringRuleRepository: RecurringRuleRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: AddRecurringBillViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle()
        recurringRuleRepository = mock()
        categoryRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AddRecurringBillViewModel {
        return AddRecurringBillViewModel(savedStateHandle, recurringRuleRepository, categoryRepository)
    }

    // ==================== Initial State Tests (2 tests) ====================

    // Test 1: Initial state has default values
    @Test
    fun `initial state has default values`() = runTest {
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.merchantName)
        assertEquals("", state.amount)
        assertEquals(RecurringFrequency.MONTHLY, state.frequency)
        assertEquals(1, state.dayOfPeriod)
        assertNull(state.selectedCategoryId)
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertNull(state.error)
    }

    // Test 2: Initial state loads categories
    @Test
    fun `initial state loads categories from repository`() = runTest {
        val categories = listOf(
            Category(id = 1L, name = "Food", emoji = "🍔"),
            Category(id = 2L, name = "Transport", emoji = "🚗")
        )
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(categories))

        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.categories.size)
    }

    // ==================== Input Validation Tests (3 tests) ====================

    // Test 3: updateMerchantName updates state
    @Test
    fun `updateMerchantName updates merchant name in state`() = runTest {
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMerchantName("Netflix")

        assertEquals("Netflix", viewModel.uiState.value.merchantName)
    }

    // Test 4: updateAmount updates state
    @Test
    fun `updateAmount updates amount in state`() = runTest {
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateAmount("649.00")

        assertEquals("649.00", viewModel.uiState.value.amount)
    }

    // Test 5: updateFrequency updates state
    @Test
    fun `updateFrequency updates frequency in state`() = runTest {
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFrequency(RecurringFrequency.WEEKLY)

        assertEquals(RecurringFrequency.WEEKLY, viewModel.uiState.value.frequency)
    }

    // ==================== Save Tests (4 tests) ====================

    // Test 6: saveBill creates rule with correct data
    @Test
    fun `saveBill creates recurring rule with entered data`() = runTest {
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))
        whenever(recurringRuleRepository.insert(any())).thenReturn(1L)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMerchantName("Netflix")
        viewModel.updateAmount("649")
        viewModel.updateFrequency(RecurringFrequency.MONTHLY)
        viewModel.updateDayOfPeriod(15)
        viewModel.selectCategory(5L)

        viewModel.saveBill()
        advanceUntilIdle()

        verify(recurringRuleRepository).insert(argThat { rule ->
            rule.merchantPattern == "Netflix" &&
            rule.expectedAmount == 649.0 &&
            rule.frequency == RecurringFrequency.MONTHLY &&
            rule.dayOfPeriod == 15 &&
            rule.categoryId == 5L &&
            rule.isUserConfirmed
        })
    }

    // Test 7: saveBill sets success state
    @Test
    fun `saveBill sets saveSuccess to true on success`() = runTest {
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))
        whenever(recurringRuleRepository.insert(any())).thenReturn(1L)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMerchantName("Test")
        viewModel.updateAmount("100")

        viewModel.saveBill()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveSuccess)
    }

    // Test 8: saveBill shows error on failure
    @Test
    fun `saveBill sets error on exception`() = runTest {
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))
        whenever(recurringRuleRepository.insert(any())).thenThrow(RuntimeException("Database error"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMerchantName("Test")
        viewModel.updateAmount("100")

        viewModel.saveBill()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }

    // Test 9: saveBill validates required fields
    @Test
    fun `saveBill shows error for empty merchant name`() = runTest {
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateAmount("100")
        // Don't set merchant name

        viewModel.saveBill()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.saveSuccess)
        verify(recurringRuleRepository, never()).insert(any())
    }

    // Test 10: saveBill validates amount
    @Test
    fun `saveBill shows error for invalid amount`() = runTest {
        whenever(categoryRepository.getAllActive()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateMerchantName("Test")
        viewModel.updateAmount("invalid")

        viewModel.saveBill()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.saveSuccess)
        verify(recurringRuleRepository, never()).insert(any())
    }
}
