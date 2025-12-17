package com.fino.app.data.repository

import com.fino.app.data.local.dao.CreditCardDao
import com.fino.app.data.local.entity.CreditCardEntity
import com.fino.app.domain.model.CreditCard
import com.fino.app.domain.model.CreditCardBill
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditCardRepository @Inject constructor(
    private val dao: CreditCardDao
) {

    fun getActiveCardsFlow(): Flow<List<CreditCard>> {
        return dao.getActiveCardsFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getActiveCards(): List<CreditCard> {
        return dao.getActiveCards().map { it.toDomain() }
    }

    suspend fun getById(id: Long): CreditCard? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun getByLastFour(lastFour: String): CreditCard? {
        return dao.getByLastFour(lastFour)?.toDomain()
    }

    suspend fun getByLastFourAndBank(lastFour: String, bankName: String): CreditCard? {
        return dao.getByLastFourAndBank(lastFour, bankName)?.toDomain()
    }

    suspend fun insert(card: CreditCard): Long {
        return dao.insert(card.toEntity())
    }

    suspend fun update(card: CreditCard) {
        dao.update(card.toEntity())
    }

    suspend fun addUnbilledAmount(cardId: Long, amount: Double) {
        dao.addUnbilledAmount(cardId, amount)
    }

    suspend fun updateBillInfo(cardId: Long, totalDue: Double, minimumDue: Double?, dueDate: LocalDate) {
        dao.updateBillInfo(cardId, totalDue, minimumDue, DateUtils.toEpochMillis(dueDate))
    }

    suspend fun getUpcomingBills(withinDays: Int = 30): List<CreditCardBill> {
        val maxDate = DateUtils.toEpochMillis(LocalDate.now().plusDays(withinDays.toLong()))
        return dao.getCardsWithUpcomingDue(maxDate).map { card ->
            CreditCardBill(
                cardLastFour = card.lastFourDigits,
                bankName = card.bankName,
                totalDue = card.previousDue,
                minimumDue = card.minimumDue,
                dueDate = card.previousDueDate?.let { DateUtils.toLocalDate(it) } ?: LocalDate.now()
            )
        }
    }

    suspend fun getActiveCardCount(): Int {
        return dao.getActiveCardCount()
    }

    private fun CreditCardEntity.toDomain(): CreditCard {
        return CreditCard(
            id = id,
            bankName = bankName,
            cardName = cardName,
            lastFourDigits = lastFourDigits,
            creditLimit = creditLimit,
            billingCycleDay = billingCycleDay,
            dueDateDay = dueDateDay,
            currentUnbilled = currentUnbilled,
            previousDue = previousDue,
            previousDueDate = previousDueDate?.let { DateUtils.toLocalDate(it) },
            minimumDue = minimumDue,
            isActive = isActive,
            createdAt = DateUtils.fromEpochMillis(createdAt)
        )
    }

    private fun CreditCard.toEntity(): CreditCardEntity {
        return CreditCardEntity(
            id = id,
            bankName = bankName,
            cardName = cardName,
            lastFourDigits = lastFourDigits,
            creditLimit = creditLimit,
            billingCycleDay = billingCycleDay,
            dueDateDay = dueDateDay,
            currentUnbilled = currentUnbilled,
            previousDue = previousDue,
            previousDueDate = previousDueDate?.let { DateUtils.toEpochMillis(it) },
            minimumDue = minimumDue,
            isActive = isActive,
            createdAt = DateUtils.toEpochMillis(createdAt)
        )
    }
}
