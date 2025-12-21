package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.FamilyMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMemberEntity>): List<Long>

    @Update
    suspend fun update(member: FamilyMemberEntity)

    @Delete
    suspend fun delete(member: FamilyMemberEntity)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getById(id: Long): FamilyMemberEntity?

    @Query("SELECT * FROM family_members ORDER BY sortOrder ASC, name ASC")
    fun getAllFlow(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<FamilyMemberEntity>

    @Query("SELECT * FROM family_members WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): FamilyMemberEntity?

    @Query("SELECT COUNT(*) FROM family_members")
    suspend fun getCount(): Int

    @Query("SELECT * FROM family_members WHERE name LIKE '%' || :query || '%' ORDER BY sortOrder ASC")
    suspend fun search(query: String): List<FamilyMemberEntity>

    @Query("UPDATE family_members SET isDefault = 0")
    suspend fun clearDefaults()

    @Query("UPDATE family_members SET isDefault = 1 WHERE id = :id")
    suspend fun setAsDefault(id: Long)

    @Query("UPDATE family_members SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("SELECT DISTINCT paidBy FROM transactions WHERE paidBy IS NOT NULL AND paidBy != '' ORDER BY paidBy ASC")
    suspend fun getDistinctPayers(): List<String>
}
