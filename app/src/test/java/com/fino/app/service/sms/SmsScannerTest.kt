package com.fino.app.service.sms

import android.content.Context
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType
import com.fino.app.service.categorization.SmartCategorizationService
import com.fino.app.service.categorization.CategorizationResult
import com.fino.app.service.parser.ParsedTransaction
import com.fino.app.service.parser.SmsParser
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.time.YearMonth

class SmsScannerTest {

    private lateinit var smsScanner: SmsScanner
    private lateinit var mockContext: Context
    private lateinit var mockSmsReader: SmsReader
    private lateinit var mockTransactionRepository: TransactionRepository
    private lateinit var mockSmsParser: SmsParser
    private lateinit var mockSmartCategorizationService: SmartCategorizationService

    @Before
    fun setup() {
        mockContext = mock()
        mockSmsReader = mock()
        mockTransactionRepository = mock()
        mockSmsParser = mock()
        mockSmartCategorizationService = mock()

        // Default categorization result
        runBlocking {
            whenever(mockSmartCategorizationService.categorize(any(), any(), any(), any())).thenReturn(
                CategorizationResult(
                    categoryId = 1L,
                    confidence = 0.9f,
                    method = "merchant_mapping",
                    suggestedName = "Test Merchant",
                    tier = 1
                )
            )
        }

        smsScanner = SmsScanner(mockContext, mockSmsReader, mockTransactionRepository, mockSmsParser, mockSmartCategorizationService)
    }

    @Test
    fun `scanMonth returns empty result when no SMS found`() = runTest {
        // Given
        whenever(mockSmsReader.readSmsForMonth(any())).thenReturn(emptyList())

        // When
        val result = smsScanner.scanMonth(YearMonth.now())

        // Then
        assertEquals(0, result.totalSmsScanned)
        assertEquals(0, result.transactionsFound)
        assertEquals(0, result.transactionsSaved)
        assertEquals(0, result.duplicatesSkipped)
    }

    @Test
    fun `scanMonth parses bank SMS and saves transactions`() = runTest {
        // Given
        val smsMessages = listOf(
            SmsMessage(
                id = "1",
                sender = "AD-HDFCBK",
                body = "Rs.500 debited from A/c XX1234 to VPA user@upi",
                timestamp = System.currentTimeMillis()
            )
        )
        val parsedTransaction = ParsedTransaction(
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "user@upi",
            transactionDate = LocalDateTime.now(),
            reference = "txn123",
            confidence = 0.9f
        )

        whenever(mockSmsReader.readSmsForMonth(any())).thenReturn(smsMessages)
        whenever(mockSmsParser.parse(any())).thenReturn(parsedTransaction)
        whenever(mockTransactionRepository.existsByRawSmsBody(any())).thenReturn(false)
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)

        // When
        val result = smsScanner.scanMonth(YearMonth.now())

        // Then
        assertEquals(1, result.totalSmsScanned)
        assertEquals(1, result.transactionsFound)
        assertEquals(1, result.transactionsSaved)
        verify(mockTransactionRepository).insert(any())
    }

    @Test
    fun `scanMonth skips non-bank SMS`() = runTest {
        // Given
        val smsMessages = listOf(
            SmsMessage(
                id = "1",
                sender = "FRIEND",
                body = "Hey, how are you?",
                timestamp = System.currentTimeMillis()
            )
        )

        whenever(mockSmsReader.readSmsForMonth(any())).thenReturn(smsMessages)

        // When
        val result = smsScanner.scanMonth(YearMonth.now())

        // Then
        assertEquals(1, result.totalSmsScanned)
        assertEquals(0, result.transactionsFound)
        assertEquals(0, result.transactionsSaved)
        verify(mockSmsParser, never()).parse(any())
    }

    @Test
    fun `scanMonth skips SMS that cannot be parsed`() = runTest {
        // Given
        val smsMessages = listOf(
            SmsMessage(
                id = "1",
                sender = "AD-HDFCBK",
                body = "Your OTP is 123456",
                timestamp = System.currentTimeMillis()
            )
        )

        whenever(mockSmsReader.readSmsForMonth(any())).thenReturn(smsMessages)
        whenever(mockSmsParser.parse(any())).thenReturn(null)

        // When
        val result = smsScanner.scanMonth(YearMonth.now())

        // Then
        assertEquals(1, result.totalSmsScanned)
        assertEquals(0, result.transactionsFound)
        assertEquals(0, result.transactionsSaved)
    }

    @Test
    fun `scanMonth skips duplicate transactions`() = runTest {
        // Given
        val smsBody = "Rs.500 debited from A/c XX1234 to VPA user@upi"
        val smsMessages = listOf(
            SmsMessage(
                id = "1",
                sender = "AD-HDFCBK",
                body = smsBody,
                timestamp = System.currentTimeMillis()
            )
        )
        val parsedTransaction = ParsedTransaction(
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "user@upi",
            transactionDate = LocalDateTime.now(),
            reference = "txn123",
            confidence = 0.9f
        )

        whenever(mockSmsReader.readSmsForMonth(any())).thenReturn(smsMessages)
        whenever(mockSmsParser.parse(any())).thenReturn(parsedTransaction)
        whenever(mockTransactionRepository.existsByRawSmsBody(smsBody)).thenReturn(true)

        // When
        val result = smsScanner.scanMonth(YearMonth.now())

        // Then
        assertEquals(1, result.totalSmsScanned)
        assertEquals(1, result.transactionsFound)
        assertEquals(0, result.transactionsSaved)
        assertEquals(1, result.duplicatesSkipped)
        verify(mockTransactionRepository, never()).insert(any())
    }

    @Test
    fun `scanMonth handles multiple bank senders`() = runTest {
        // Given
        val smsMessages = listOf(
            SmsMessage("1", "AD-HDFCBK", "Rs.100 debited", System.currentTimeMillis()),
            SmsMessage("2", "VM-SBIINB", "Rs.200 debited", System.currentTimeMillis()),
            SmsMessage("3", "BZ-ICICIB", "Rs.300 debited", System.currentTimeMillis()),
            SmsMessage("4", "JD-PAYTM", "Rs.400 paid", System.currentTimeMillis())
        )

        whenever(mockSmsReader.readSmsForMonth(any())).thenReturn(smsMessages)
        whenever(mockSmsParser.parse(any())).thenReturn(
            ParsedTransaction(
                amount = 100.0,
                type = TransactionType.DEBIT,
                merchantName = "test",
                transactionDate = LocalDateTime.now(),
                confidence = 0.9f
            )
        )
        whenever(mockTransactionRepository.existsByRawSmsBody(any())).thenReturn(false)
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)

        // When
        val result = smsScanner.scanMonth(YearMonth.now())

        // Then
        assertEquals(4, result.totalSmsScanned)
        assertEquals(4, result.transactionsFound)
        verify(mockTransactionRepository, times(4)).insert(any())
    }

    @Test
    fun `scanMonth sets correct transaction source as SMS_SCAN`() = runTest {
        // Given
        val smsMessages = listOf(
            SmsMessage(
                id = "1",
                sender = "AD-HDFCBK",
                body = "Rs.500 debited",
                timestamp = System.currentTimeMillis()
            )
        )
        val parsedTransaction = ParsedTransaction(
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "merchant",
            transactionDate = LocalDateTime.now(),
            confidence = 0.9f,
            reference = null
        )

        whenever(mockSmsReader.readSmsForMonth(any())).thenReturn(smsMessages)
        whenever(mockSmsParser.parse(any())).thenReturn(parsedTransaction)
        whenever(mockTransactionRepository.existsByRawSmsBody(any())).thenReturn(false)
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)

        // When
        smsScanner.scanMonth(YearMonth.now())

        // Then
        val captor = argumentCaptor<Transaction>()
        verify(mockTransactionRepository).insert(captor.capture())
        assertEquals(TransactionSource.SMS_SCAN, captor.firstValue.source)
    }

    @Test
    fun `isBankSms correctly identifies bank senders`() {
        assertTrue(smsScanner.isBankSms("AD-HDFCBK"))
        assertTrue(smsScanner.isBankSms("VM-SBIINB"))
        assertTrue(smsScanner.isBankSms("BZ-AXISBK"))
        assertTrue(smsScanner.isBankSms("JD-PAYTM"))
        assertTrue(smsScanner.isBankSms("AM-GPAY"))
        assertTrue(smsScanner.isBankSms("SOMEBANK"))
        assertTrue(smsScanner.isBankSms("UPI-123"))

        assertFalse(smsScanner.isBankSms("FRIEND"))
        assertFalse(smsScanner.isBankSms("+919876543210"))
        assertFalse(smsScanner.isBankSms("DOMINOS"))
    }

    @Test
    fun `scanMonth continues processing after individual SMS error`() = runTest {
        // Given
        val smsMessages = listOf(
            SmsMessage("1", "AD-HDFCBK", "Rs.100 debited", System.currentTimeMillis()),
            SmsMessage("2", "AD-HDFCBK", "Rs.200 debited", System.currentTimeMillis())
        )

        whenever(mockSmsReader.readSmsForMonth(any())).thenReturn(smsMessages)
        whenever(mockSmsParser.parse("Rs.100 debited")).thenThrow(RuntimeException("Parse error"))
        whenever(mockSmsParser.parse("Rs.200 debited")).thenReturn(
            ParsedTransaction(
                amount = 200.0,
                type = TransactionType.DEBIT,
                merchantName = "test",
                transactionDate = LocalDateTime.now(),
                confidence = 0.9f
            )
        )
        whenever(mockTransactionRepository.existsByRawSmsBody(any())).thenReturn(false)
        whenever(mockTransactionRepository.insert(any())).thenReturn(1L)

        // When
        val result = smsScanner.scanMonth(YearMonth.now())

        // Then
        assertEquals(2, result.totalSmsScanned)
        assertEquals(1, result.transactionsFound)
        assertEquals(1, result.transactionsSaved)
        assertEquals(1, result.errors)
    }
}
