package com.fino.app.data.repository

import com.fino.app.data.local.dao.BillDao
import com.fino.app.data.local.entity.BillEntity
import com.fino.app.domain.model.Bill
import com.fino.app.domain.model.BillEntitySource
import com.fino.app.domain.model.BillEntityStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class BillRepositoryTest {

    private lateinit var dao: BillDao
    private lateinit var repository: BillRepository

    @Before
    fun setup() {
        dao = mock()
        repository = BillRepository(dao)
    }

    private fun entity(
        id: Long = 0,
        accountId: Long? = null,
        creditCardId: Long? = null,
        cycleEnd: Long = 1_700_000_000_000L,
        dueDate: Long = 1_700_000_000_000L,
        totalDue: Double = 5000.0,
        status: BillEntityStatus = BillEntityStatus.PENDING
    ): BillEntity = BillEntity(
        id = id,
        accountId = accountId,
        creditCardId = creditCardId,
        cycleStart = cycleEnd - 30L * 86_400_000L,
        cycleEnd = cycleEnd,
        dueDate = dueDate,
        totalDue = totalDue,
        minDue = 500.0,
        paidAt = null,
        paidAmount = null,
        status = status,
        source = BillEntitySource.CC_STATEMENT,
        payeeVpa = null,
        payeeName = null,
        updatedAt = 0L
    )

    @Test
    fun `getAllFlow maps entities to domain`() = runTest {
        whenever(dao.getAllFlow()).thenReturn(flowOf(listOf(entity(id = 1))))
        val result = repository.getAllFlow().first()
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    @Test
    fun `getUpcomingBillsFlow queries dao with computed window`() = runTest {
        whenever(dao.getUpcomingFlow(any(), any())).thenReturn(flowOf(listOf(entity(id = 2))))
        val result = repository.getUpcomingBillsFlow(withinDays = 7).first()
        assertEquals(1, result.size)
        assertEquals(2L, result[0].id)
    }

    @Test
    fun `getByCreditCard returns mapped bills`() = runTest {
        whenever(dao.getByCreditCard(99L)).thenReturn(listOf(entity(id = 3, creditCardId = 99L)))
        val result = repository.getByCreditCard(99L)
        assertEquals(1, result.size)
        assertEquals(99L, result[0].creditCardId)
    }

    @Test
    fun `getById returns null when dao returns null`() = runTest {
        whenever(dao.getById(404L)).thenReturn(null)
        assertNull(repository.getById(404L))
    }

    @Test
    fun `upsertForCycle updates existing row when match found`() = runTest {
        val existing = entity(id = 10, accountId = 7L, cycleEnd = 1234L)
        whenever(dao.findByAccountAndCycle(7L, 1234L)).thenReturn(existing)

        val bill = Bill(
            accountId = 7L,
            cycleStart = 0L,
            cycleEnd = 1234L,
            dueDate = 1500L,
            totalDue = 9000.0
        )
        val id = repository.upsertForCycle(bill)

        assertEquals(10L, id)
        verify(dao).update(any())
        verify(dao, never()).insert(any())
    }

    @Test
    fun `upsertForCycle inserts new row when no match found`() = runTest {
        whenever(dao.findByAccountAndCycle(any(), any())).thenReturn(null)
        whenever(dao.insert(any())).thenReturn(42L)

        val bill = Bill(
            accountId = 7L,
            cycleStart = 0L,
            cycleEnd = 1234L,
            dueDate = 1500L,
            totalDue = 9000.0
        )
        val id = repository.upsertForCycle(bill)

        assertEquals(42L, id)
        verify(dao).insert(any())
        verify(dao, never()).update(any())
    }

    @Test
    fun `markPaid sets PAID when paid amount covers total`() = runTest {
        val stored = entity(id = 5, totalDue = 5000.0)
        whenever(dao.getById(5L)).thenReturn(stored)

        repository.markPaid(5L, paidAmount = 5000.0, paidAt = 111L)

        verify(dao).markPaid(
            id = eq(5L),
            paidAt = eq(111L),
            paidAmount = eq(5000.0),
            status = eq(BillEntityStatus.PAID),
            updatedAt = any()
        )
    }

    @Test
    fun `markPaid sets PARTIAL when paid amount is less than total`() = runTest {
        val stored = entity(id = 6, totalDue = 5000.0)
        whenever(dao.getById(6L)).thenReturn(stored)

        repository.markPaid(6L, paidAmount = 2000.0, paidAt = 222L)

        verify(dao).markPaid(
            id = eq(6L),
            paidAt = eq(222L),
            paidAmount = eq(2000.0),
            status = eq(BillEntityStatus.PARTIAL),
            updatedAt = any()
        )
    }

    @Test
    fun `markPaid is a no-op when bill missing`() = runTest {
        whenever(dao.getById(any())).thenReturn(null)
        repository.markPaid(999L, paidAmount = 100.0)
        verify(dao, never()).markPaid(any(), any(), any(), any(), any())
    }
}
