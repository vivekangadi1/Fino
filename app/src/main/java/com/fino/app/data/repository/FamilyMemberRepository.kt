package com.fino.app.data.repository

import com.fino.app.data.local.dao.FamilyMemberDao
import com.fino.app.data.local.entity.FamilyMemberEntity
import com.fino.app.domain.model.FamilyMember
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyMemberRepository @Inject constructor(
    private val dao: FamilyMemberDao
) {

    fun getAllFlow(): Flow<List<FamilyMember>> {
        return dao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getAll(): List<FamilyMember> {
        return dao.getAll().map { it.toDomain() }
    }

    suspend fun getById(id: Long): FamilyMember? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun getDefault(): FamilyMember? {
        return dao.getDefault()?.toDomain()
    }

    suspend fun insert(member: FamilyMember): Long {
        return dao.insert(member.toEntity())
    }

    suspend fun insertAll(members: List<FamilyMember>): List<Long> {
        return dao.insertAll(members.map { it.toEntity() })
    }

    suspend fun update(member: FamilyMember) {
        dao.update(member.toEntity())
    }

    suspend fun delete(member: FamilyMember) {
        dao.delete(member.toEntity())
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun getCount(): Int {
        return dao.getCount()
    }

    suspend fun search(query: String): List<FamilyMember> {
        return dao.search(query).map { it.toDomain() }
    }

    suspend fun setAsDefault(id: Long) {
        dao.clearDefaults()
        dao.setAsDefault(id)
    }

    suspend fun updateSortOrder(id: Long, sortOrder: Int) {
        dao.updateSortOrder(id, sortOrder)
    }

    /**
     * Get all distinct payers from transaction history
     */
    suspend fun getDistinctPayers(): List<String> {
        return dao.getDistinctPayers()
    }

    /**
     * Create default family members
     */
    suspend fun createDefaults(): List<Long> {
        val defaults = FamilyMember.DEFAULT_MEMBERS.mapIndexed { index, (name, relationship) ->
            FamilyMember(
                name = name,
                relationship = relationship,
                isDefault = index == 0, // First one (Self) is default
                sortOrder = index,
                createdAt = LocalDateTime.now()
            )
        }
        return insertAll(defaults)
    }

    /**
     * Get all payer names (from both FamilyMember table and distinct transaction payers)
     */
    suspend fun getAllPayerNames(): List<String> {
        val familyMembers = getAll().map { it.name }
        val transactionPayers = getDistinctPayers()
        return (familyMembers + transactionPayers).distinct().sorted()
    }

    private fun FamilyMemberEntity.toDomain(): FamilyMember {
        return FamilyMember(
            id = id,
            name = name,
            relationship = relationship,
            isDefault = isDefault,
            sortOrder = sortOrder,
            createdAt = DateUtils.fromEpochMillis(createdAt)
        )
    }

    private fun FamilyMember.toEntity(): FamilyMemberEntity {
        return FamilyMemberEntity(
            id = id,
            name = name,
            relationship = relationship,
            isDefault = isDefault,
            sortOrder = sortOrder,
            createdAt = DateUtils.toEpochMillis(createdAt)
        )
    }
}
