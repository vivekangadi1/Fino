package com.fino.app.data.repository

import com.fino.app.data.local.dao.EventMemberDao
import com.fino.app.data.local.entity.EventMemberEntity
import com.fino.app.domain.model.EventMember
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class EventMemberRepositoryTest {

    private lateinit var dao: EventMemberDao
    private lateinit var repository: EventMemberRepository

    @Before
    fun setup() {
        dao = mock()
        repository = EventMemberRepository(dao)
    }

    private fun entity(id: Long = 1, eventId: Long = 10, name: String = "Alice") = EventMemberEntity(
        id = id,
        eventId = eventId,
        name = name,
        avatarSeed = name,
        sharePercent = 0f,
        isPayer = false,
        createdAt = 0L
    )

    @Test
    fun `getByEventFlow maps entities to domain`() = runTest {
        whenever(dao.getByEventFlow(10L))
            .thenReturn(flowOf(listOf(entity(name = "Alice"), entity(id = 2, name = "Rahul"))))
        val result = repository.getByEventFlow(10L).first()
        assertEquals(2, result.size)
        assertEquals("Alice", result[0].name)
        assertEquals("Rahul", result[1].name)
    }

    @Test
    fun `getByEvent maps entities to domain`() = runTest {
        whenever(dao.getByEvent(10L)).thenReturn(listOf(entity()))
        val result = repository.getByEvent(10L)
        assertEquals(1, result.size)
        assertEquals(10L, result[0].eventId)
    }

    @Test
    fun `getById returns mapped domain when present`() = runTest {
        whenever(dao.getById(1L)).thenReturn(entity())
        val result = repository.getById(1L)
        assertNotNull(result)
        assertEquals(1L, result?.id)
    }

    @Test
    fun `getById returns null when absent`() = runTest {
        whenever(dao.getById(any())).thenReturn(null)
        assertNull(repository.getById(999L))
    }

    @Test
    fun `insert round-trips via dao`() = runTest {
        whenever(dao.insert(any())).thenReturn(55L)
        val member = EventMember(eventId = 1L, name = "Aman", avatarSeed = "Aman")
        val id = repository.insert(member)
        assertEquals(55L, id)
    }

    @Test
    fun `insertAll round-trips via dao`() = runTest {
        whenever(dao.insertAll(any())).thenReturn(listOf(1L, 2L))
        val members = listOf(
            EventMember(eventId = 10L, name = "A", avatarSeed = "A"),
            EventMember(eventId = 10L, name = "B", avatarSeed = "B")
        )
        val ids = repository.insertAll(members)
        assertEquals(listOf(1L, 2L), ids)
    }

    @Test
    fun `markAsPayer clears previous payer then sets new one`() = runTest {
        repository.markAsPayer(memberId = 3L, eventId = 10L)
        val inOrder = inOrder(dao)
        inOrder.verify(dao).clearPayerForEvent(10L)
        inOrder.verify(dao).setPayer(3L)
    }

    @Test
    fun `deleteByEvent forwards to dao`() = runTest {
        repository.deleteByEvent(7L)
        verify(dao).deleteByEvent(7L)
    }
}
