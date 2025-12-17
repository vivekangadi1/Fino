package com.fino.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Extension functions for common operations.
 */

// String extensions
fun String.normalizeForMatching(): String {
    return this.uppercase()
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun String.toTitleCase(): String {
    return this.split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

// LocalDateTime extensions
fun LocalDateTime.toEpochMillis(): Long = DateUtils.toEpochMillis(this)

fun Long.toLocalDateTime(): LocalDateTime = DateUtils.fromEpochMillis(this)

// LocalDate extensions
fun LocalDate.toEpochMillis(): Long = DateUtils.toEpochMillis(this)

fun Long.toLocalDate(): LocalDate = DateUtils.toLocalDate(this)

// YearMonth extensions
fun YearMonth.toDisplayString(): String = DateUtils.formatMonthYear(this)

// Double extensions
fun Double.formatAsCurrency(): String = AmountFormatter.format(this)

fun Double.formatAsCompactCurrency(): String = AmountFormatter.formatCompact(this)

// List extensions
inline fun <T> List<T>.sumByDouble(selector: (T) -> Double): Double {
    var sum = 0.0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
