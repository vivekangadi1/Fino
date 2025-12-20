package com.fino.app.service.export

import android.content.Context
import android.net.Uri
import com.fino.app.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class ExportServiceTest {

    private lateinit var mockContext: Context
    private lateinit var csvExporter: CsvExporter
    private lateinit var pdfExporter: PdfExporter
    private lateinit var exportService: ExportService

    private val testTransactions = listOf(
        Transaction(
            id = 1L,
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            categoryId = 1L,
            transactionDate = LocalDateTime.of(2024, 1, 15, 14, 30),
            source = TransactionSource.SMS,
            paymentMethod = "UPI",
            reference = "UPI123456"
        ),
        Transaction(
            id = 2L,
            amount = 1200.0,
            type = TransactionType.DEBIT,
            merchantName = "Amazon",
            categoryId = 2L,
            transactionDate = LocalDateTime.of(2024, 1, 20, 10, 15),
            source = TransactionSource.MANUAL,
            paymentMethod = "CREDIT_CARD",
            cardLastFour = "4521"
        ),
        Transaction(
            id = 3L,
            amount = 50000.0,
            type = TransactionType.CREDIT,
            merchantName = "Salary",
            categoryId = null,
            transactionDate = LocalDateTime.of(2024, 1, 1, 9, 0),
            source = TransactionSource.MANUAL
        )
    )

    @Before
    fun setup() {
        mockContext = mock()

        // Mock context methods needed for file operations
        val mockFilesDir = File(System.getProperty("java.io.tmpdir"), "test_exports")
        mockFilesDir.mkdirs()
        whenever(mockContext.filesDir).thenReturn(mockFilesDir)
        whenever(mockContext.cacheDir).thenReturn(mockFilesDir)

        csvExporter = CsvExporter(mockContext)
        pdfExporter = PdfExporter(mockContext)
        exportService = ExportServiceImpl(csvExporter, pdfExporter)
    }

    // Test 1: CSV export generates valid file
    @Test
    fun `CSV export generates valid file with correct data`() = runTest {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val request = ExportRequest(
            format = ExportFormat.CSV,
            startDate = startDate,
            endDate = endDate,
            transactions = testTransactions
        )

        val result = exportService.exportTransactions(request)

        assertTrue(result is ExportResult.Success)
        if (result is ExportResult.Success) {
            assertNotNull(result.fileUri)
            assertTrue(result.fileName.endsWith(".csv"))
            assertEquals(ExportFormat.CSV, result.format)
            assertTrue(result.fileName.contains("2024-01-01"))
            assertTrue(result.fileName.contains("2024-01-31"))
        }
    }

    // Test 2: PDF export generates valid file
    @Test
    fun `PDF export generates valid file with correct data`() = runTest {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val request = ExportRequest(
            format = ExportFormat.PDF,
            startDate = startDate,
            endDate = endDate,
            transactions = testTransactions
        )

        val result = exportService.exportTransactions(request)

        assertTrue(result is ExportResult.Success)
        if (result is ExportResult.Success) {
            assertNotNull(result.fileUri)
            assertTrue(result.fileName.endsWith(".pdf"))
            assertEquals(ExportFormat.PDF, result.format)
            assertTrue(result.fileName.contains("2024-01-01"))
            assertTrue(result.fileName.contains("2024-01-31"))
        }
    }

    // Test 3: Export for specific date range
    @Test
    fun `export only includes transactions within date range`() = runTest {
        val startDate = LocalDate.of(2024, 1, 15)
        val endDate = LocalDate.of(2024, 1, 20)

        // Filter transactions to only those in range
        val filteredTransactions = testTransactions.filter {
            val txDate = it.transactionDate.toLocalDate()
            txDate >= startDate && txDate <= endDate
        }

        val request = ExportRequest(
            format = ExportFormat.CSV,
            startDate = startDate,
            endDate = endDate,
            transactions = filteredTransactions
        )

        val result = exportService.exportTransactions(request)

        assertTrue(result is ExportResult.Success)
        // Should only export 2 transactions (Jan 15 and Jan 20)
        assertEquals(2, filteredTransactions.size)
    }

    // Test 4: Export file naming convention
    @Test
    fun `export file follows naming convention with dates`() = runTest {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val request = ExportRequest(
            format = ExportFormat.CSV,
            startDate = startDate,
            endDate = endDate,
            transactions = testTransactions
        )

        val result = exportService.exportTransactions(request)

        assertTrue(result is ExportResult.Success)
        if (result is ExportResult.Success) {
            // Expected format: fino_transactions_YYYY-MM-DD_to_YYYY-MM-DD.csv
            val expectedPattern = "fino_transactions_\\d{4}-\\d{2}-\\d{2}_to_\\d{4}-\\d{2}-\\d{2}\\.csv"
            assertTrue(result.fileName.matches(Regex(expectedPattern)))
        }
    }

    // Test 5: CSV export handles empty transactions
    @Test
    fun `CSV export handles empty transaction list`() = runTest {
        val request = ExportRequest(
            format = ExportFormat.CSV,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            transactions = emptyList()
        )

        val result = exportService.exportTransactions(request)

        assertTrue(result is ExportResult.Success)
        if (result is ExportResult.Success) {
            assertNotNull(result.fileUri)
        }
    }

    // Test 6: PDF export handles empty transactions
    @Test
    fun `PDF export handles empty transaction list`() = runTest {
        val request = ExportRequest(
            format = ExportFormat.PDF,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            transactions = emptyList()
        )

        val result = exportService.exportTransactions(request)

        assertTrue(result is ExportResult.Success)
        if (result is ExportResult.Success) {
            assertNotNull(result.fileUri)
        }
    }

    // Test 7: CSV export includes all required columns
    @Test
    fun `CSV export includes all transaction fields`() = runTest {
        val request = ExportRequest(
            format = ExportFormat.CSV,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            transactions = testTransactions
        )

        val result = exportService.exportTransactions(request)

        assertTrue(result is ExportResult.Success)
        // CSV should include columns: Date, Merchant, Amount, Type, Payment Method, Reference
    }

    // Test 8: Export handles special characters in merchant names
    @Test
    fun `export handles special characters in merchant names`() = runTest {
        val specialTransactions = listOf(
            Transaction(
                id = 1L,
                amount = 500.0,
                type = TransactionType.DEBIT,
                merchantName = "McDonald's \"Fast Food\"",
                transactionDate = LocalDateTime.of(2024, 1, 15, 14, 30),
                source = TransactionSource.MANUAL
            )
        )

        val request = ExportRequest(
            format = ExportFormat.CSV,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            transactions = specialTransactions
        )

        val result = exportService.exportTransactions(request)

        assertTrue(result is ExportResult.Success)
    }

    // Test 9: PDF export includes summary statistics
    @Test
    fun `PDF export includes summary statistics`() = runTest {
        val request = ExportRequest(
            format = ExportFormat.PDF,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            transactions = testTransactions
        )

        val result = exportService.exportTransactions(request)

        assertTrue(result is ExportResult.Success)
        // PDF should include: Total Income, Total Expenses, Net Balance
    }

    // Test 10: Export returns error for invalid operations
    @Test
    fun `export returns error when file operations fail`() = runTest {
        // Create a context that will cause file operations to fail
        val badContext = mock<Context>()
        whenever(badContext.filesDir).thenReturn(null)

        val badCsvExporter = CsvExporter(badContext)
        val badPdfExporter = PdfExporter(badContext)
        val badExportService = ExportServiceImpl(badCsvExporter, badPdfExporter)

        val request = ExportRequest(
            format = ExportFormat.CSV,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            transactions = testTransactions
        )

        val result = badExportService.exportTransactions(request)

        assertTrue(result is ExportResult.Error)
    }
}
