package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: CreditCardEntity): Long

    @Update
    suspend fun update(card: CreditCardEntity)

    @Delete
    suspend fun delete(card: CreditCardEntity)

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getById(id: Long): CreditCardEntity?

    @Query("SELECT * FROM credit_cards WHERE lastFourDigits = :lastFour LIMIT 1")
    suspend fun getByLastFour(lastFour: String): CreditCardEntity?

    @Query("SELECT * FROM credit_cards WHERE lastFourDigits = :lastFour AND bankName = :bankName LIMIT 1")
    suspend fun getByLastFourAndBank(lastFour: String, bankName: String): CreditCardEntity?

    @Query("SELECT * FROM credit_cards WHERE isActive = 1 ORDER BY bankName")
    fun getActiveCardsFlow(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE isActive = 1 ORDER BY bankName")
    suspend fun getActiveCards(): List<CreditCardEntity>

    @Query("SELECT * FROM credit_cards ORDER BY bankName")
    suspend fun getAllCards(): List<CreditCardEntity>

    @Query("UPDATE credit_cards SET currentUnbilled = currentUnbilled + :amount WHERE id = :cardId")
    suspend fun addUnbilledAmount(cardId: Long, amount: Double)

    @Query("UPDATE credit_cards SET previousDue = :totalDue, minimumDue = :minimumDue, previousDueDate = :dueDate, currentUnbilled = 0 WHERE id = :cardId")
    suspend fun updateBillInfo(cardId: Long, totalDue: Double, minimumDue: Double?, dueDate: Long)

    @Query("SELECT * FROM credit_cards WHERE isActive = 1 AND previousDueDate IS NOT NULL AND previousDueDate <= :maxDate")
    suspend fun getCardsWithUpcomingDue(maxDate: Long): List<CreditCardEntity>

    @Query("SELECT COUNT(*) FROM credit_cards WHERE isActive = 1")
    suspend fun getActiveCardCount(): Int
}
