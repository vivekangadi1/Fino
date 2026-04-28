package com.fino.app.domain.model

import com.fino.app.data.local.entity.AccountEntity

enum class AccountType {
    BANK,
    CARD,
    WALLET,
    CASH
}

enum class AccountSource {
    SMS,
    AA,
    MANUAL
}

data class Account(
    val id: Long = 0,
    val type: AccountType,
    val institution: String,
    val displayName: String,
    val maskedNumber: String? = null,
    val paymentMethod: String? = null,
    val balance: Double? = null,
    val currency: String = "INR",
    val syncSource: AccountSource = AccountSource.SMS,
    val lastSyncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    type = type,
    institution = institution,
    displayName = displayName,
    maskedNumber = maskedNumber,
    paymentMethod = paymentMethod,
    balance = balance,
    currency = currency,
    syncSource = syncSource,
    lastSyncedAt = lastSyncedAt,
    createdAt = createdAt
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    type = type,
    institution = institution,
    displayName = displayName,
    maskedNumber = maskedNumber,
    paymentMethod = paymentMethod,
    balance = balance,
    currency = currency,
    syncSource = syncSource,
    lastSyncedAt = lastSyncedAt,
    createdAt = createdAt
)
