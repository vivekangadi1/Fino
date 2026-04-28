package com.fino.app.service.upi

import android.net.Uri
import java.util.Locale

/**
 * Builders for `upi://pay` deep-links consumable by any installed UPI app
 * (GPay, PhonePe, Paytm, etc.). Any installed app that resolves the scheme
 * can complete the payment; no SDK integration required.
 *
 * Spec reference: NPCI "Common UPI URL Scheme". Required params: `pa`, `pn`, `am`, `cu`.
 * `tn` (note) and `tr` (transaction reference) are strongly recommended.
 */
object UpiDeepLink {

    fun buildPayUri(
        payeeVpa: String,
        payeeName: String,
        amount: Double,
        note: String,
        txnRef: String,
        currency: String = "INR"
    ): Uri = Uri.Builder()
        .scheme("upi")
        .authority("pay")
        .appendQueryParameter("pa", payeeVpa)
        .appendQueryParameter("pn", payeeName)
        .appendQueryParameter("am", String.format(Locale.US, "%.2f", amount))
        .appendQueryParameter("tn", note)
        .appendQueryParameter("tr", txnRef)
        .appendQueryParameter("cu", currency)
        .build()
}
