package com.fino.app.data.repository

import com.fino.app.data.local.dao.AccountDao
import com.fino.app.data.local.dao.TransactionDao
import com.fino.app.domain.model.Account
import com.fino.app.domain.model.TransactionType
import com.fino.app.domain.model.toDomain
import com.fino.app.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val dao: AccountDao,
    private val transactionDao: TransactionDao
) {

    fun getAllFlow(): Flow<List<Account>> =
        dao.getAllFlow().map { list -> list.map { it.toDomain() } }

    suspend fun getAll(): List<Account> = dao.getAll().map { it.toDomain() }

    suspend fun getById(id: Long): Account? = dao.getById(id)?.toDomain()

    suspend fun getByPaymentSignature(
        paymentMethod: String?,
        institution: String?,
        maskedNumber: String?
    ): Account? = dao.findBySignature(paymentMethod, institution, maskedNumber)?.toDomain()

    suspend fun insert(account: Account): Long = dao.insert(account.toEntity())

    suspend fun update(account: Account) = dao.update(account.toEntity())

    suspend fun updateBalance(id: Long, balance: Double) {
        dao.updateBalance(id, balance, System.currentTimeMillis())
    }

    suspend fun recomputeBalanceFromTransactions(id: Long) {
        val txns = transactionDao.getTransactionsByAccountId(id)
        val balance = txns.fold(0.0) { acc, t ->
            if (t.type == TransactionType.CREDIT) acc + t.amount else acc - t.amount
        }
        dao.updateBalance(id, balance, System.currentTimeMillis())
    }
}
