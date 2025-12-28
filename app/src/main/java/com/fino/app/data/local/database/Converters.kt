package com.fino.app.data.local.database

import androidx.room.TypeConverter
import com.fino.app.domain.model.AchievementType
import com.fino.app.domain.model.EMIStatus
import com.fino.app.domain.model.EventStatus
import com.fino.app.domain.model.LoanStatus
import com.fino.app.domain.model.LoanType
import com.fino.app.domain.model.PaymentStatus
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.domain.model.TransactionSource
import com.fino.app.domain.model.TransactionType

class Converters {

    // TransactionType
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    // TransactionSource
    @TypeConverter
    fun fromTransactionSource(source: TransactionSource): String = source.name

    @TypeConverter
    fun toTransactionSource(value: String): TransactionSource = TransactionSource.valueOf(value)

    // RecurringFrequency
    @TypeConverter
    fun fromRecurringFrequency(frequency: RecurringFrequency): String = frequency.name

    @TypeConverter
    fun toRecurringFrequency(value: String): RecurringFrequency = RecurringFrequency.valueOf(value)

    // AchievementType
    @TypeConverter
    fun fromAchievementType(type: AchievementType): String = type.name

    @TypeConverter
    fun toAchievementType(value: String): AchievementType = AchievementType.valueOf(value)

    // EventStatus
    @TypeConverter
    fun fromEventStatus(status: EventStatus): String = status.name

    @TypeConverter
    fun toEventStatus(value: String): EventStatus = EventStatus.valueOf(value)

    // PaymentStatus
    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String = status.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = PaymentStatus.valueOf(value)

    // EMIStatus
    @TypeConverter
    fun fromEMIStatus(status: EMIStatus): String = status.name

    @TypeConverter
    fun toEMIStatus(value: String): EMIStatus = EMIStatus.valueOf(value)

    // LoanStatus
    @TypeConverter
    fun fromLoanStatus(status: LoanStatus): String = status.name

    @TypeConverter
    fun toLoanStatus(value: String): LoanStatus = LoanStatus.valueOf(value)

    // LoanType
    @TypeConverter
    fun fromLoanType(type: LoanType): String = type.name

    @TypeConverter
    fun toLoanType(value: String): LoanType = LoanType.valueOf(value)
}
