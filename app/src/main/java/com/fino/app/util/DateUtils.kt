package com.fino.app.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * Utility class for date/time operations.
 */
object DateUtils {

    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val displayTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    private val displayDateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    /**
     * Convert LocalDateTime to epoch milliseconds.
     */
    fun toEpochMillis(dateTime: LocalDateTime): Long {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Convert epoch milliseconds to LocalDateTime.
     */
    fun fromEpochMillis(millis: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
    }

    /**
     * Convert LocalDate to epoch milliseconds (at start of day).
     */
    fun toEpochMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Convert epoch milliseconds to LocalDate.
     */
    fun toLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    /**
     * Format date for display.
     */
    fun formatDate(date: LocalDate): String = date.format(displayDateFormatter)

    /**
     * Format date time for display.
     */
    fun formatDateTime(dateTime: LocalDateTime): String = dateTime.format(displayDateTimeFormatter)

    /**
     * Format time for display.
     */
    fun formatTime(dateTime: LocalDateTime): String = dateTime.format(displayTimeFormatter)

    /**
     * Format month and year.
     */
    fun formatMonthYear(yearMonth: YearMonth): String = yearMonth.format(monthYearFormatter)

    /**
     * Get the start of a month as epoch millis.
     */
    fun getMonthStart(yearMonth: YearMonth): Long {
        return toEpochMillis(yearMonth.atDay(1).atStartOfDay())
    }

    /**
     * Get the end of a month as epoch millis.
     */
    fun getMonthEnd(yearMonth: YearMonth): Long {
        return toEpochMillis(yearMonth.atEndOfMonth().atTime(23, 59, 59))
    }

    /**
     * Get current YearMonth.
     */
    fun currentYearMonth(): YearMonth = YearMonth.now()

    /**
     * Convert YearMonth to string format "YYYY-MM".
     */
    fun yearMonthToString(yearMonth: YearMonth): String {
        return yearMonth.toString() // Already in YYYY-MM format
    }

    /**
     * Parse YearMonth from string "YYYY-MM".
     */
    fun stringToYearMonth(str: String): YearMonth {
        return YearMonth.parse(str)
    }

    /**
     * Check if a date is today.
     */
    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    /**
     * Check if a date is yesterday.
     */
    fun isYesterday(date: LocalDate): Boolean = date == LocalDate.now().minusDays(1)

    /**
     * Get a human-readable relative date string.
     */
    fun getRelativeDate(date: LocalDate): String {
        return when {
            isToday(date) -> "Today"
            isYesterday(date) -> "Yesterday"
            date.isAfter(LocalDate.now().minusDays(7)) -> date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
            else -> formatDate(date)
        }
    }
}
