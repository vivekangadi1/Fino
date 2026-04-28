package com.fino.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fino.app.domain.model.NoticeType

@Entity(
    tableName = "notices",
    indices = [
        Index("period"),
        Index(value = ["period", "rankOrder"])
    ]
)
data class NoticesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val period: String,
    val type: NoticeType,
    val title: String,
    val body: String,
    val isWarn: Boolean = false,
    val routeJson: String? = null,
    val chartDataJson: String? = null,
    val rankOrder: Int = 0,
    val computedAt: Long
)
