package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fino.app.domain.model.AccountSource
import com.fino.app.domain.model.AccountType

@Entity(
    tableName = "accounts",
    indices = [
        Index("type"),
        Index("institution"),
        Index("paymentMethod"),
        Index(value = ["paymentMethod", "institution", "maskedNumber"], unique = false)
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
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
    val createdAt: Long
)
