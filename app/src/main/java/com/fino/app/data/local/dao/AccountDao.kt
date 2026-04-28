package com.fino.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fino.app.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("SELECT * FROM accounts ORDER BY createdAt ASC")
    fun getAllFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY createdAt ASC")
    suspend fun getAll(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query(
        """
        SELECT * FROM accounts
        WHERE (:paymentMethod IS NULL OR paymentMethod = :paymentMethod)
          AND (:institution IS NULL OR institution = :institution)
          AND (:maskedNumber IS NULL OR maskedNumber IS :maskedNumber)
        LIMIT 1
        """
    )
    suspend fun findBySignature(
        paymentMethod: String?,
        institution: String?,
        maskedNumber: String?
    ): AccountEntity?

    @Query("UPDATE accounts SET balance = :balance, lastSyncedAt = :syncedAt WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Double, syncedAt: Long)
}
