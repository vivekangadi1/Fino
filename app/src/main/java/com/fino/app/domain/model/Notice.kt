package com.fino.app.domain.model

import com.fino.app.data.local.entity.NoticesEntity

enum class NoticeType {
    CATEGORY_CHANGE,
    MERCHANT_RISE,
    BILL_DUE,
    SUBS,
    NEW_MERCHANTS,
    WEEKEND,
    PACE,
    CASHBACK,
    LARGEST_TXN,
    SPIKE_DAY
}

data class Notice(
    val id: Long = 0,
    val period: String,
    val type: NoticeType,
    val title: String,
    val body: String,
    val isWarn: Boolean = false,
    val routeJson: String? = null,
    val chartDataJson: String? = null,
    val rankOrder: Int = 0,
    val computedAt: Long = System.currentTimeMillis()
)

fun NoticesEntity.toDomain(): Notice = Notice(
    id = id,
    period = period,
    type = type,
    title = title,
    body = body,
    isWarn = isWarn,
    routeJson = routeJson,
    chartDataJson = chartDataJson,
    rankOrder = rankOrder,
    computedAt = computedAt
)

fun Notice.toEntity(): NoticesEntity = NoticesEntity(
    id = id,
    period = period,
    type = type,
    title = title,
    body = body,
    isWarn = isWarn,
    routeJson = routeJson,
    chartDataJson = chartDataJson,
    rankOrder = rankOrder,
    computedAt = computedAt
)
