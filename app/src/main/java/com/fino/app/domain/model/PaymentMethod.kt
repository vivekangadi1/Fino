package com.fino.app.domain.model

/**
 * Payment method used for a transaction
 */
enum class PaymentMethod(val displayName: String, val value: String) {
    UPI("UPI", "UPI"),
    CREDIT_CARD("Credit Card", "CREDIT_CARD"),
    DEBIT_CARD("Debit Card", "DEBIT_CARD"),
    CASH("Cash", "CASH"),
    NET_BANKING("Net Banking", "NET_BANKING"),
    OTHER("Other", "OTHER");

    companion object {
        fun fromString(value: String?): PaymentMethod? {
            return when (value) {
                "UPI" -> UPI
                "CREDIT_CARD" -> CREDIT_CARD
                "DEBIT_CARD" -> DEBIT_CARD
                "CASH" -> CASH
                "NET_BANKING" -> NET_BANKING
                "OTHER" -> OTHER
                else -> null
            }
        }
    }
}
