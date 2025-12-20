package com.fino.app.data.repository

import com.fino.app.data.local.dao.TransactionDao
import com.fino.app.data.local.entity.TransactionEntity
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryTest {

    private lateinit var mockDao: TransactionDao
    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        mockDao = mock()
        repository = TransactionRepository(mockDao)
    }

    @Test
    fun `insert transaction with payment method fields persists correctly`() = runTest {
        val transaction = Transaction(
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            transactionDate = LocalDateTime.now(),
            source = TransactionSource.SMS,
            bankName = "HDFC",
            paymentMethod = "UPI",
            cardLastFour = null
        )

        whenever(mockDao.insert(any())).thenReturn(1L)

        val id = repository.insert(transaction)

        assertEquals(1L, id)

        // Verify that DAO insert was called with entity containing payment method fields
        verify(mockDao).insert(argThat { entity ->
            entity.bankName == "HDFC" &&
            entity.paymentMethod == "UPI" &&
            entity.cardLastFour == null
        })
    }

    @Test
    fun `insert credit card transaction with card last four persists correctly`() = runTest {
        val transaction = Transaction(
            amount = 1000.0,
            type = TransactionType.DEBIT,
            merchantName = "Amazon",
            transactionDate = LocalDateTime.now(),
            source = TransactionSource.SMS,
            bankName = "ICICI",
            paymentMethod = "CREDIT_CARD",
            cardLastFour = "1234"
        )

        whenever(mockDao.insert(any())).thenReturn(1L)

        repository.insert(transaction)

        verify(mockDao).insert(argThat { entity ->
            entity.bankName == "ICICI" &&
            entity.paymentMethod == "CREDIT_CARD" &&
            entity.cardLastFour == "1234"
        })
    }

    @Test
    fun `insert transaction with null payment method fields persists correctly`() = runTest {
        val transaction = Transaction(
            amount = 200.0,
            type = TransactionType.DEBIT,
            merchantName = "Cash Payment",
            transactionDate = LocalDateTime.now(),
            source = TransactionSource.MANUAL,
            bankName = null,
            paymentMethod = null,
            cardLastFour = null
        )

        whenever(mockDao.insert(any())).thenReturn(1L)

        repository.insert(transaction)

        verify(mockDao).insert(argThat { entity ->
            entity.bankName == null &&
            entity.paymentMethod == null &&
            entity.cardLastFour == null
        })
    }

    @Test
    fun `getById returns transaction with payment method fields`() = runTest {
        val now = LocalDateTime.now()
        val nowEpoch = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val entity = TransactionEntity(
            id = 1L,
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            transactionDate = nowEpoch,
            createdAt = nowEpoch,
            source = TransactionSource.SMS,
            bankName = "HDFC",
            paymentMethod = "UPI",
            cardLastFour = null
        )

        whenever(mockDao.getById(1L)).thenReturn(entity)

        val transaction = repository.getById(1L)

        assertNotNull(transaction)
        assertEquals("HDFC", transaction?.bankName)
        assertEquals("UPI", transaction?.paymentMethod)
        assertNull(transaction?.cardLastFour)
    }

    @Test
    fun `update transaction with payment method fields persists changes`() = runTest {
        val transaction = Transaction(
            id = 1L,
            amount = 500.0,
            type = TransactionType.DEBIT,
            merchantName = "Swiggy",
            transactionDate = LocalDateTime.now(),
            source = TransactionSource.SMS,
            bankName = "SBI",
            paymentMethod = "UPI",
            cardLastFour = null
        )

        repository.update(transaction)

        verify(mockDao).update(argThat { entity ->
            entity.id == 1L &&
            entity.bankName == "SBI" &&
            entity.paymentMethod == "UPI" &&
            entity.cardLastFour == null
        })
    }

    @Test
    fun `entity to domain mapping preserves payment method fields`() = runTest {
        val now = LocalDateTime.now()
        val nowEpoch = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val entity = TransactionEntity(
            id = 1L,
            amount = 1000.0,
            type = TransactionType.DEBIT,
            merchantName = "Amazon",
            transactionDate = nowEpoch,
            createdAt = nowEpoch,
            source = TransactionSource.SMS,
            bankName = "HDFC",
            paymentMethod = "CREDIT_CARD",
            cardLastFour = "5678"
        )

        whenever(mockDao.getById(1L)).thenReturn(entity)

        val transaction = repository.getById(1L)

        assertNotNull(transaction)
        assertEquals("HDFC", transaction?.bankName)
        assertEquals("CREDIT_CARD", transaction?.paymentMethod)
        assertEquals("5678", transaction?.cardLastFour)
        assertEquals("Amazon", transaction?.merchantName)
        assertEquals(1000.0, transaction?.amount)
    }

    @Test
    fun `domain to entity mapping preserves payment method fields`() = runTest {
        val transaction = Transaction(
            id = 5L,
            amount = 300.0,
            type = TransactionType.DEBIT,
            merchantName = "Uber",
            transactionDate = LocalDateTime.now(),
            source = TransactionSource.SMS,
            bankName = "AXIS",
            paymentMethod = "UPI",
            cardLastFour = null
        )

        repository.update(transaction)

        verify(mockDao).update(argThat { entity ->
            entity.id == 5L &&
            entity.amount == 300.0 &&
            entity.merchantName == "Uber" &&
            entity.bankName == "AXIS" &&
            entity.paymentMethod == "UPI" &&
            entity.cardLastFour == null
        })
    }
}
