package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.EMIEntity
import com.fino.app.domain.model.EMIStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EMIDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(emi: EMIEntity): Long

    @Update
    suspend fun update(emi: EMIEntity)

    @Delete
    suspend fun delete(emi: EMIEntity)

    @Query("SELECT * FROM emis WHERE id = :id")
    suspend fun getById(id: Long): EMIEntity?

    @Query("SELECT * FROM emis WHERE status = 'ACTIVE' ORDER BY nextDueDate ASC")
    fun getActiveEMIsFlow(): Flow<List<EMIEntity>>

    @Query("SELECT * FROM emis WHERE status = 'ACTIVE' ORDER BY nextDueDate ASC")
    suspend fun getActiveEMIs(): List<EMIEntity>

    @Query("SELECT * FROM emis ORDER BY createdAt DESC")
    fun getAllEMIsFlow(): Flow<List<EMIEntity>>

    @Query("SELECT * FROM emis WHERE creditCardId = :cardId ORDER BY nextDueDate ASC")
    suspend fun getEMIsByCard(cardId: Long): List<EMIEntity>

    @Query("SELECT * FROM emis WHERE creditCardId = :cardId AND status = 'ACTIVE' ORDER BY nextDueDate ASC")
    fun getActiveEMIsByCardFlow(cardId: Long): Flow<List<EMIEntity>>

    @Query("SELECT * FROM emis WHERE status = 'COMPLETED' ORDER BY endDate DESC")
    fun getCompletedEMIsFlow(): Flow<List<EMIEntity>>

    @Query("SELECT SUM(monthlyAmount) FROM emis WHERE status = 'ACTIVE'")
    suspend fun getTotalMonthlyEMI(): Double?

    @Query("SELECT SUM(monthlyAmount) FROM emis WHERE creditCardId = :cardId AND status = 'ACTIVE'")
    suspend fun getTotalMonthlyEMIByCard(cardId: Long): Double?

    @Query("SELECT COUNT(*) FROM emis WHERE status = 'ACTIVE'")
    suspend fun getActiveEMICount(): Int

    @Query("UPDATE emis SET paidCount = paidCount + 1, nextDueDate = :nextDueDate WHERE id = :id")
    suspend fun recordPayment(id: Long, nextDueDate: Long)

    @Query("UPDATE emis SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: EMIStatus)

    @Query("SELECT * FROM emis WHERE nextDueDate BETWEEN :startDate AND :endDate AND status = 'ACTIVE'")
    suspend fun getUpcomingEMIs(startDate: Long, endDate: Long): List<EMIEntity>
}
