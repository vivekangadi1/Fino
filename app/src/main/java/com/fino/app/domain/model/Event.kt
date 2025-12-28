package com.fino.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Domain model for an expense tracking event (trip, wedding, renovation, etc.)
 *
 * @property id Unique identifier
 * @property name Display name of the event
 * @property description Optional description
 * @property emoji Visual emoji for the event
 * @property eventTypeId Foreign key to EventType
 * @property budgetAmount Optional budget limit
 * @property alertAt75 Whether to alert at 75% budget usage
 * @property alertAt100 Whether to alert at 100% budget usage
 * @property startDate When the event starts
 * @property endDate When the event ends (null = ongoing)
 * @property status Current status of the event
 * @property isActive Whether the event is active (soft delete)
 * @property createdAt When this event was created
 * @property updatedAt When this event was last updated
 */
data class Event(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val emoji: String,
    val eventTypeId: Long,
    val budgetAmount: Double? = null,
    val alertAt75: Boolean = true,
    val alertAt100: Boolean = true,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val status: EventStatus = EventStatus.ACTIVE,
    val isActive: Boolean = true,
    val excludeFromMainTotals: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Whether this event has a budget set
     */
    val hasBudget: Boolean
        get() = budgetAmount != null && budgetAmount > 0

    /**
     * Whether the event is currently ongoing (active and within date range)
     */
    val isOngoing: Boolean
        get() {
            val today = LocalDate.now()
            return status == EventStatus.ACTIVE &&
                   startDate <= today &&
                   (endDate == null || endDate >= today)
        }

    /**
     * Number of days the event has been running (or total days if completed)
     */
    val durationDays: Long
        get() {
            val end = endDate ?: LocalDate.now()
            return ChronoUnit.DAYS.between(startDate, end) + 1
        }

    /**
     * Days remaining until event ends (null if no end date)
     */
    val daysRemaining: Long?
        get() = endDate?.let {
            val today = LocalDate.now()
            if (it >= today) ChronoUnit.DAYS.between(today, it) else 0
        }

    /**
     * Days elapsed since event started
     */
    val daysElapsed: Long
        get() {
            val today = LocalDate.now()
            return ChronoUnit.DAYS.between(startDate, today).coerceAtLeast(1)
        }
}
