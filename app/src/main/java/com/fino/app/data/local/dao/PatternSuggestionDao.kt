package com.fino.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fino.app.data.local.entity.PatternSuggestionEntity
import com.fino.app.data.local.entity.SuggestionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternSuggestionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(suggestion: PatternSuggestionEntity): Long

    @Update
    suspend fun update(suggestion: PatternSuggestionEntity)

    @Query("SELECT * FROM pattern_suggestions WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingSuggestionsFlow(): Flow<List<PatternSuggestionEntity>>

    @Query("SELECT * FROM pattern_suggestions WHERE status = 'PENDING'")
    suspend fun getPendingSuggestions(): List<PatternSuggestionEntity>

    @Query("SELECT * FROM pattern_suggestions WHERE id = :id")
    suspend fun getById(id: Long): PatternSuggestionEntity?

    @Query("SELECT * FROM pattern_suggestions WHERE merchantPattern = :pattern LIMIT 1")
    suspend fun findByMerchantPattern(pattern: String): PatternSuggestionEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM pattern_suggestions WHERE merchantPattern = :pattern AND status != 'DISMISSED')")
    suspend fun existsByMerchantPattern(pattern: String): Boolean

    @Query("UPDATE pattern_suggestions SET status = :status, dismissedAt = :dismissedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SuggestionStatus, dismissedAt: Long? = null)

    @Query("DELETE FROM pattern_suggestions WHERE status = 'DISMISSED' AND dismissedAt < :before")
    suspend fun cleanupOldDismissed(before: Long)

    @Query("SELECT COUNT(*) FROM pattern_suggestions WHERE status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>

    @Query("DELETE FROM pattern_suggestions WHERE id = :id")
    suspend fun delete(id: Long)
}
