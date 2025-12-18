package com.fino.app.presentation.viewmodel

import com.fino.app.service.sms.SmsScanResult
import com.fino.app.service.sms.SmsScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class SmsScanViewModelTest {

    private lateinit var viewModel: SmsScanViewModel
    private lateinit var mockSmsScanner: SmsScanner

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockSmsScanner = mock()
        viewModel = SmsScanViewModel(mockSmsScanner)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() {
        val state = viewModel.uiState.value
        assertFalse(state.isScanning)
        assertNull(state.lastResult)
        assertNull(state.error)
    }

    @Test
    fun `scanCurrentMonth sets isScanning to true during scan`() = runTest {
        // Given
        whenever(mockSmsScanner.scanMonth(any())).thenReturn(
            SmsScanResult(10, 5, 4, 1, 0)
        )

        // When
        viewModel.scanCurrentMonth()

        // Then - scanning should be in progress
        assertTrue(viewModel.uiState.value.isScanning)

        // Complete the coroutine
        advanceUntilIdle()

        // Then - scanning should be complete
        assertFalse(viewModel.uiState.value.isScanning)
    }

    @Test
    fun `scanCurrentMonth calls scanner with current month`() = runTest {
        // Given
        whenever(mockSmsScanner.scanMonth(any())).thenReturn(
            SmsScanResult(10, 5, 4, 1, 0)
        )

        // When
        viewModel.scanCurrentMonth()
        advanceUntilIdle()

        // Then
        verify(mockSmsScanner).scanMonth(YearMonth.now())
    }

    @Test
    fun `scanCurrentMonth updates state with result`() = runTest {
        // Given
        val expectedResult = SmsScanResult(
            totalSmsScanned = 100,
            transactionsFound = 25,
            transactionsSaved = 20,
            duplicatesSkipped = 5,
            errors = 0
        )
        whenever(mockSmsScanner.scanMonth(any())).thenReturn(expectedResult)

        // When
        viewModel.scanCurrentMonth()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(expectedResult, state.lastResult)
        assertNull(state.error)
    }

    @Test
    fun `scanCurrentMonth handles errors gracefully`() = runTest {
        // Given
        whenever(mockSmsScanner.scanMonth(any())).thenThrow(RuntimeException("Permission denied"))

        // When
        viewModel.scanCurrentMonth()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isScanning)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Permission denied"))
    }

    @Test
    fun `scanMonth scans specific month`() = runTest {
        // Given
        val targetMonth = YearMonth.of(2024, 6)
        whenever(mockSmsScanner.scanMonth(any())).thenReturn(
            SmsScanResult(50, 10, 8, 2, 0)
        )

        // When
        viewModel.scanMonth(targetMonth)
        advanceUntilIdle()

        // Then
        verify(mockSmsScanner).scanMonth(targetMonth)
    }

    @Test
    fun `clearError clears the error state`() = runTest {
        // Given - simulate an error
        whenever(mockSmsScanner.scanMonth(any())).thenThrow(RuntimeException("Error"))
        viewModel.scanCurrentMonth()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `cannot start new scan while scanning`() = runTest {
        // Given
        whenever(mockSmsScanner.scanMonth(any())).thenAnswer {
            Thread.sleep(100) // Simulate slow scan
            SmsScanResult(10, 5, 4, 1, 0)
        }

        // When - try to start two scans
        viewModel.scanCurrentMonth()
        viewModel.scanCurrentMonth() // Should be ignored

        advanceUntilIdle()

        // Then - scanner should only be called once
        verify(mockSmsScanner, times(1)).scanMonth(any())
    }
}
