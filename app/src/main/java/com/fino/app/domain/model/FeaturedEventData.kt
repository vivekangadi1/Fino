package com.fino.app.domain.model

data class FeaturedEventData(
    val summary: EventSummary,
    val participantCount: Int,
    val yourShare: Double,
    val categorySegments: List<CategorySegment>,
    val settleRows: List<EventSettleRow>
)

data class CategorySegment(
    val categoryName: String,
    val amount: Double,
    val fraction: Float,
    val paletteIndex: Int
)

data class EventSettleRow(
    val who: String,
    val owesYou: Boolean,
    val amount: Double
)
