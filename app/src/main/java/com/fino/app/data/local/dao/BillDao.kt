package com.fino.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fino.app.data.local.entity.BillEntity
import com.fino.app.domain.model.BillEntityStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: BillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bills: List<BillEntity>): List<Long>

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getById(id: Long): BillEntity?

    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    fun getAllFlow(): Flow<List<BillEntity>>

    @Query(
        """
        SELECT * FROM bills
        WHERE dueDate BETWEEN :now AND :cutoff
        ORDER BY dueDate ASC
        """
    )
    fun getUpcomingFlow(now: Long, cutoff: Long): Flow<List<BillEntity>>

    @Query(
        """
        SELECT * FROM bills
        WHERE dueDate BETWEEN :now AND :cutoff
        ORDER BY dueDate ASC
        """
    )
    suspend fun getUpcoming(now: Long, cutoff: Long): List<BillEntity>

    @Query("SELECT * FROM bills WHERE creditCardId = :creditCardId ORDER BY cycleEnd DESC")
    suspend fun getByCreditCard(creditCardId: Long): List<BillEntity>

    @Query("SELECT * FROM bills WHERE accountId = :accountId ORDER BY cycleEnd DESC")
    suspend fun getByAccount(accountId: Long): List<BillEntity>

    @Query("SELECT * FROM bills WHERE status = :status ORDER BY dueDate ASC")
    suspend fun getByStatus(status: BillEntityStatus): List<BillEntity>

    @Query(
        """
        SELECT * FROM bills
        WHERE (accountId IS :accountId OR :accountId IS NULL)
          AND cycleEnd = :cycleEnd
        LIMIT 1
        """
    )
    suspend fun findByAccountAndCycle(accountId: Long?, cycleEnd: Long): BillEntity?

    @Query(
        """
        UPDATE bills
        SET paidAt = :paidAt,
            paidAmount = :paidAmount,
            status = :status,
            updatedAt = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun markPaid(id: Long, paidAt: Long, paidAmount: Double, status: BillEntityStatus, updatedAt: Long)
}
