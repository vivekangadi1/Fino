package com.fino.app.data.repository

import com.fino.app.data.local.dao.AccountDao
import com.fino.app.data.local.dao.TransactionDao
import com.fino.app.data.local.entity.AccountEntity
import com.fino.app.data.local.entity.TransactionEntity
import com.fino.app.domain.model.Account
import com.fino.app.domain.model.AccountSource
import com.fino.app.domain.model.AccountType
import com.fino.app.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class AccountRepositoryTest {

    private lateinit var accountDao: AccountDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var repository: AccountRepository

    @Before
    fun setup() {
        accountDao = mock()
        transactionDao = mock()
        repository = AccountRepository(accountDao, transactionDao)
    }

    private fun accountEntity(id: Long = 1L, name: String = "HDFC Savings") = AccountEntity(
        id = id,
        type = AccountType.BANK,
        institution = "HDFC",
        displayName = name,
        maskedNumber = "1234",
        paymentMethod = "UPI",
        balance = 0.0,
        currency = "INR",
        syncSource = AccountSource.SMS,
        lastSyncedAt = null,
        createdAt = 0L
    )

    private fun txnEntity(amount: Double, type: TransactionType, accountId: Long = 1L) = TransactionEntity(
        id = 0L,
        amount = amount,
        type = type,
        merchantName = "Test",
        transactionDate = 0L,
        createdAt = 0L,
        accountId = accountId
    )

    @Test
    fun `getAllFlow maps entities to domain`() = runTest {
        whenever(accountDao.getAllFlow()).thenReturn(flowOf(listOf(accountEntity())))
        val result = repository.getAllFlow().first()
        assertEquals(1, result.size)
        assertEquals("HDFC Savings", result[0].displayName)
    }

    @Test
    fun `getByPaymentSignature maps single result`() = runTest {
        whenever(accountDao.findBySignature("UPI", "HDFC", "1234"))
            .thenReturn(accountEntity())
        val result = repository.getByPaymentSignature("UPI", "HDFC", "1234")
        assertNotNull(result)
        assertEquals("HDFC", result?.institution)
    }

    @Test
    fun `getByPaymentSignature returns null when dao returns null`() = runTest {
        whenever(accountDao.findBySignature(any(), any(), any())).thenReturn(null)
        val result = repository.getByPaymentSignature("UPI", "ICICI", "9999")
        assertNull(result)
    }

    @Test
    fun `recomputeBalanceFromTransactions sums credits minus debits`() = runTest {
        val txns = listOf(
            txnEntity(1000.0, TransactionType.CREDIT),
            txnEntity(250.0, TransactionType.DEBIT),
            txnEntity(500.0, TransactionType.CREDIT),
            txnEntity(100.0, TransactionType.DEBIT)
        )
        whenever(transactionDao.getTransactionsByAccountId(1L)).thenReturn(txns)

        repository.recomputeBalanceFromTransactions(1L)

        // 1000 + 500 - 250 - 100 = 1150
        verify(accountDao).updateBalance(eq(1L), eq(1150.0), any())
    }

    @Test
    fun `recomputeBalanceFromTransactions handles empty list`() = runTest {
        whenever(transactionDao.getTransactionsByAccountId(99L)).thenReturn(emptyList())
        repository.recomputeBalanceFromTransactions(99L)
        verify(accountDao).updateBalance(eq(99L), eq(0.0), any())
    }

    @Test
    fun `insert round-trips via dao`() = runTest {
        val account = Account(
            type = AccountType.CARD,
            institution = "ICICI",
            displayName = "ICICI CC"
        )
        whenever(accountDao.insert(any())).thenReturn(17L)
        val id = repository.insert(account)
        assertEquals(17L, id)
        verify(accountDao).insert(any())
    }
}
