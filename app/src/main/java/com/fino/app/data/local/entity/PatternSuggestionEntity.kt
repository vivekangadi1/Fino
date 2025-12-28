package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fino.app.domain.model.RecurringFrequency

/**
 * Status of a pattern suggestion
 */
enum class SuggestionStatus {
    PENDING,    // User has not acted
    CONFIRMED,  // User confirmed, RecurringRule created
    DISMISSED   // User dismissed
}

/**
 * Source of pattern suggestion
 */
enum class SuggestionSource {
    SMS_SUBSCRIPTION,    // Detected from SMS subscription pattern (e.g., Netflix, Spotify)
    PATTERN_DETECTION    // Detected from historical pattern analysis
}

/**
 * Persistent storage for pattern suggestions.
 * Suggestions are created when:
 * 1. SMS parser detects a subscription payment (isLikelySubscription = true)
 * 2. Background pattern detection finds recurring patterns
 */
@Entity(
    tableName = "pattern_suggestions",
    indices = [
        Index("merchantPattern", unique = true),
        Index("status")
    ]
)
data class PatternSuggestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchantPattern: String,
    val displayName: String,
    val averageAmount: Double,
    val frequency: RecurringFrequency,
    val typicalDayOfPeriod: Int,
    val occurrenceCount: Int,
    val confidence: Float,
    val nextExpected: Long,  // Epoch millis
    val categoryId: Long?,
    val status: SuggestionStatus = SuggestionStatus.PENDING,
    val source: SuggestionSource,
    val createdAt: Long,  // Epoch millis
    val dismissedAt: Long? = null  // Epoch millis
)
