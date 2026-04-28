package com.fino.app.data.repository

import com.fino.app.data.local.dao.EventMemberDao
import com.fino.app.domain.model.EventMember
import com.fino.app.domain.model.toDomain
import com.fino.app.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventMemberRepository @Inject constructor(
    private val dao: EventMemberDao
) {

    fun getByEventFlow(eventId: Long): Flow<List<EventMember>> =
        dao.getByEventFlow(eventId).map { list -> list.map { it.toDomain() } }

    suspend fun getByEvent(eventId: Long): List<EventMember> =
        dao.getByEvent(eventId).map { it.toDomain() }

    suspend fun getById(id: Long): EventMember? = dao.getById(id)?.toDomain()

    suspend fun insert(member: EventMember): Long = dao.insert(member.toEntity())

    suspend fun insertAll(members: List<EventMember>): List<Long> =
        dao.insertAll(members.map { it.toEntity() })

    suspend fun update(member: EventMember) = dao.update(member.toEntity())

    suspend fun delete(member: EventMember) = dao.delete(member.toEntity())

    suspend fun deleteByEvent(eventId: Long) = dao.deleteByEvent(eventId)

    suspend fun markAsPayer(memberId: Long, eventId: Long) {
        dao.clearPayerForEvent(eventId)
        dao.setPayer(memberId)
    }
}
