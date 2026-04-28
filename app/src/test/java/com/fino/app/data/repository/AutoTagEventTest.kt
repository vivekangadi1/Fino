package com.fino.app.data.repository

import com.fino.app.data.local.dao.EventDao
import com.fino.app.data.local.entity.EventEntity
import com.fino.app.domain.model.EventStatus
import com.fino.app.util.DateUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate

class AutoTagEventTest {

    private lateinit var eventDao: EventDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var eventTypeRepository: EventTypeRepository
    private lateinit var repository: EventRepository

    @Before
    fun setup() {
        eventDao = mock()
        transactionRepository = mock()
        eventTypeRepository = mock()
        repository = EventRepository(eventDao, transactionRepository, eventTypeRepository)
    }

    private fun tripEntity(
        id: Long = 1,
        start: LocalDate,
        end: LocalDate?,
        autoTag: Boolean = true
    ) = EventEntity(
        id = id,
        name = "Trip",
        emoji = "🌴",
        eventTypeId = 1L,
        startDate = DateUtils.toEpochMillis(start),
        endDate = end?.let { DateUtils.toEpochMillis(it) },
        status = EventStatus.ACTIVE,
        isActive = true,
        excludeFromMainTotals = false,
        autoTagTransactions = autoTag,
        createdAt = 0L,
        updatedAt = 0L
    )

    @Test
    fun `returns event when date falls inside autoTag range`() = runTest {
        val target = LocalDate.of(2026, 4, 19)
        val match = tripEntity(
            id = 7L,
            start = LocalDate.of(2026, 4, 10),
            end = LocalDate.of(2026, 4, 25)
        )
        whenever(eventDao.getAutoTagEventForDate(DateUtils.toEpochMillis(target)))
            .thenReturn(match)

        val result = repository.getAutoTagEventForDate(target)

        assertNotNull(result)
        assertEquals(7L, result?.id)
        assertEquals(true, result?.autoTagTransactions)
    }

    @Test
    fun `returns null when no event matches`() = runTest {
        val target = LocalDate.of(2026, 1, 1)
        whenever(eventDao.getAutoTagEventForDate(any())).thenReturn(null)

        val result = repository.getAutoTagEventForDate(target)

        assertNull(result)
    }

    @Test
    fun `queries dao with correct epoch millis`() = runTest {
        val target = LocalDate.of(2026, 6, 15)
        whenever(eventDao.getAutoTagEventForDate(any())).thenReturn(null)

        repository.getAutoTagEventForDate(target)

        verify(eventDao).getAutoTagEventForDate(DateUtils.toEpochMillis(target))
    }
}
