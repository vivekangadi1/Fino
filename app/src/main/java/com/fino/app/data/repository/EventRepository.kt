package com.fino.app.data.repository

import com.fino.app.data.local.dao.EventDao
import com.fino.app.data.local.entity.EventEntity
import com.fino.app.domain.model.Event
import com.fino.app.domain.model.EventBudgetStatus
import com.fino.app.domain.model.EventStatus
import com.fino.app.domain.model.EventSummary
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val transactionRepository: TransactionRepository,
    private val eventTypeRepository: EventTypeRepository
) {

    fun getAllActiveEventsFlow(): Flow<List<Event>> {
        return eventDao.getAllActiveFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun getAllActiveEvents(): List<Event> {
        return eventDao.getAllActive().map { it.toDomain() }
    }

    fun getActiveEventsFlow(): Flow<List<Event>> {
        return eventDao.getActiveEventsFlow().map { list -> list.map { it.toDomain() } }
    }

    fun getMostRecentActiveEventFlow(): Flow<Event?> {
        return eventDao.getMostRecentActiveFlow().map { it?.toDomain() }
    }

    suspend fun getById(id: Long): Event? {
        return eventDao.getById(id)?.toDomain()
    }

    fun getByIdFlow(id: Long): Flow<Event?> {
        return eventDao.getByIdFlow(id).map { it?.toDomain() }
    }

    suspend fun insert(event: Event): Long {
        return eventDao.insert(event.toEntity())
    }

    suspend fun update(event: Event) {
        eventDao.update(event.toEntity())
    }

    suspend fun delete(event: Event) {
        eventDao.delete(event.toEntity())
    }

    suspend fun deleteById(eventId: Long) {
        eventDao.deleteById(eventId)
    }

    suspend fun updateStatus(eventId: Long, status: EventStatus) {
        val updatedAt = DateUtils.toEpochMillis(LocalDateTime.now())
        eventDao.updateStatus(eventId, status, updatedAt)
    }

    suspend fun completeEvent(eventId: Long) {
        updateStatus(eventId, EventStatus.COMPLETED)
    }

    /**
     * Get event summaries with spending totals and transaction counts.
     * Combines flows from events, transactions, and event types.
     */
    fun getEventSummariesFlow(): Flow<List<EventSummary>> {
        return combine(
            eventDao.getAllActiveFlow(),
            transactionRepository.getAllTransactionsFlow(),
            eventTypeRepository.getAllActiveFlow()
        ) { events, transactions, eventTypes ->
            val eventTypeMap = eventTypes.associateBy { it.id }

            events.map { eventEntity ->
                val event = eventEntity.toDomain()
                val eventTypeName = eventTypeMap[event.eventTypeId]?.name ?: "Unknown"

                // Calculate spending for this event
                val eventTransactions = transactions.filter { it.eventId == event.id }
                val totalSpent = eventTransactions.sumOf { it.amount }
                val transactionCount = eventTransactions.size

                // Calculate budget status if event has a budget
                val budgetStatus = event.budgetAmount?.let {
                    EventBudgetStatus.calculate(event, totalSpent)
                }

                EventSummary(
                    event = event,
                    eventTypeName = eventTypeName,
                    totalSpent = totalSpent,
                    transactionCount = transactionCount,
                    budgetStatus = budgetStatus
                )
            }
        }
    }

    /**
     * Get a single event summary
     */
    suspend fun getEventSummary(eventId: Long): EventSummary? {
        val eventEntity = eventDao.getById(eventId) ?: return null
        val event = eventEntity.toDomain()

        val eventType = eventTypeRepository.getById(event.eventTypeId)
        val eventTypeName = eventType?.name ?: "Unknown"

        val eventTransactions = transactionRepository.getTransactionsForEvent(eventId)
        val totalSpent = eventTransactions.sumOf { it.amount }
        val transactionCount = eventTransactions.size

        val budgetStatus = event.budgetAmount?.let {
            EventBudgetStatus.calculate(event, totalSpent)
        }

        return EventSummary(
            event = event,
            eventTypeName = eventTypeName,
            totalSpent = totalSpent,
            transactionCount = transactionCount,
            budgetStatus = budgetStatus
        )
    }

    /**
     * Get events active on a specific date
     */
    suspend fun getEventsForDate(date: LocalDate): List<Event> {
        val dateMillis = DateUtils.toEpochMillis(date)
        return eventDao.getEventsForDate(dateMillis).map { it.toDomain() }
    }

    private fun EventEntity.toDomain(): Event {
        return Event(
            id = id,
            name = name,
            description = description,
            emoji = emoji,
            eventTypeId = eventTypeId,
            budgetAmount = budgetAmount,
            alertAt75 = alertAt75,
            alertAt100 = alertAt100,
            startDate = DateUtils.toLocalDate(startDate),
            endDate = endDate?.let { DateUtils.toLocalDate(it) },
            status = status,
            isActive = isActive,
            createdAt = DateUtils.fromEpochMillis(createdAt),
            updatedAt = DateUtils.fromEpochMillis(updatedAt)
        )
    }

    private fun Event.toEntity(): EventEntity {
        return EventEntity(
            id = id,
            name = name,
            description = description,
            emoji = emoji,
            eventTypeId = eventTypeId,
            budgetAmount = budgetAmount,
            alertAt75 = alertAt75,
            alertAt100 = alertAt100,
            startDate = DateUtils.toEpochMillis(startDate),
            endDate = endDate?.let { DateUtils.toEpochMillis(it) },
            status = status,
            isActive = isActive,
            createdAt = DateUtils.toEpochMillis(createdAt),
            updatedAt = DateUtils.toEpochMillis(updatedAt)
        )
    }
}
