package com.fino.app.service.upi

/**
 * Per-issuer credit-card bill-pay VPA mapping. Values are the public
 * bill-pay handles banks publish for UPI settlement; update if an issuer
 * rotates their handle (no crash — `resolve()` returns null on miss).
 *
 * Sources: each bank's "Pay credit card bill via UPI" FAQ as of 2025-Q4.
 * Cardholder must still enter their 16-digit card number on the UPI app —
 * this mapping just routes to the correct biller.
 */
object CcBillPayeeVpa {

    data class Payee(val vpa: String, val payeeName: String)

    private val table: Map<String, Payee> = mapOf(
        "HDFC" to Payee("HDFCBK.RAZORPAY@HDFCBANK", "HDFC Credit Card"),
        "ICICI" to Payee("ccpay.icici@icici", "ICICI Credit Card"),
        "AXIS" to Payee("cc.axisbank@axisbank", "Axis Credit Card"),
        "SBI" to Payee("sbicard.0000000000@sbi", "SBI Credit Card"),
        "KOTAK" to Payee("ccpay@kotak", "Kotak Credit Card"),
        "AMEX" to Payee("AEBCCardpayments@sc", "American Express Card")
    )

    /**
     * Returns the known bill-pay Payee for the given bank token, or null
     * if the bank isn't mapped yet. The bank argument is normalised
     * case-insensitively and matched on prefix.
     */
    fun resolve(bank: String?): Payee? {
        if (bank.isNullOrBlank()) return null
        val upper = bank.uppercase()
        return table.entries.firstOrNull { (key, _) -> upper.contains(key) }?.value
    }
}
