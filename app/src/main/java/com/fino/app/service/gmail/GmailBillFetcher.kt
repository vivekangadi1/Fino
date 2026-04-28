package com.fino.app.service.gmail

import android.util.Base64
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartBody
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls credit-card statement emails from Gmail via the REST API. Uses a
 * single bank -> issuer-domain map so a new issuer can be onboarded by adding
 * its domain + a GmailBillParser dispatch branch.
 */
@Singleton
class GmailBillFetcher @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class FetchedEmail(
        val bank: String,
        val from: String,
        val subject: String,
        val body: String,
        val internalDateMillis: Long
    )

    suspend fun fetchStatementEmails(
        userEmail: String,
        sinceMillis: Long,
        maxPerIssuer: Int = 10
    ): List<FetchedEmail> = withContext(Dispatchers.IO) {
        val gmail = buildGmailClient(userEmail) ?: return@withContext emptyList()
        val sinceSeconds = sinceMillis / 1000L
        val results = mutableListOf<FetchedEmail>()

        for ((bank, domains) in ISSUER_DOMAINS) {
            val fromClause = domains.joinToString(" OR ") { "from:$it" }
            val query = "($fromClause) subject:(statement OR bill) after:$sinceSeconds"
            val list = runCatching {
                gmail.users().messages().list(USER_ID)
                    .setQ(query)
                    .setMaxResults(maxPerIssuer.toLong())
                    .execute()
            }.getOrNull() ?: continue

            val messageRefs = list.messages ?: continue
            for (ref in messageRefs) {
                val message = runCatching {
                    gmail.users().messages().get(USER_ID, ref.id)
                        .setFormat("full")
                        .execute()
                }.getOrNull() ?: continue

                val fromHeader = headerValue(message, "From") ?: continue
                val subject = headerValue(message, "Subject") ?: ""
                val body = extractBody(message)
                results += FetchedEmail(
                    bank = bank,
                    from = fromHeader,
                    subject = subject,
                    body = body,
                    internalDateMillis = message.internalDate ?: 0L
                )
            }
        }
        results
    }

    private fun buildGmailClient(userEmail: String): Gmail? {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(GmailScopes.GMAIL_READONLY)
        ).apply {
            selectedAccountName = userEmail
        }
        return runCatching {
            Gmail.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(APP_NAME)
                .build()
        }.getOrNull()
    }

    private fun headerValue(message: Message, name: String): String? {
        val headers = message.payload?.headers ?: return null
        return headers.firstOrNull { it.name.equals(name, ignoreCase = true) }?.value
    }

    private fun extractBody(message: Message): String {
        val payload = message.payload ?: return message.snippet ?: ""
        val htmlParts = mutableListOf<String>()
        val textParts = mutableListOf<String>()
        collectParts(payload, htmlParts, textParts)
        val raw = (htmlParts + textParts).firstOrNull { it.isNotBlank() }
        return raw ?: (message.snippet ?: "")
    }

    private fun collectParts(
        part: MessagePart,
        html: MutableList<String>,
        text: MutableList<String>
    ) {
        val mime = part.mimeType ?: ""
        val data = decode(part.body)
        when {
            mime.equals("text/html", true) && data.isNotBlank() -> html += data
            mime.equals("text/plain", true) && data.isNotBlank() -> text += data
        }
        part.parts?.forEach { collectParts(it, html, text) }
    }

    private fun decode(body: MessagePartBody?): String {
        val data = body?.data ?: return ""
        val bytes = runCatching { Base64.decode(data, Base64.URL_SAFE) }.getOrNull() ?: return ""
        return String(bytes, Charsets.UTF_8)
    }

    companion object {
        private const val USER_ID = "me"
        private const val APP_NAME = "Fino"

        private val ISSUER_DOMAINS = linkedMapOf(
            "HDFC" to listOf("alerts.hdfcbank.net", "hdfcbank.net", "alerts@hdfcbank.net"),
            "ICICI" to listOf("icicibank.com", "no-reply@icicibank.com"),
            "AXIS" to listOf("axisbank.com", "customercare@axisbank.com"),
            "SBI" to listOf("sbicard.com", "sbi.co.in"),
            "KOTAK" to listOf("kotak.com"),
            "AMEX" to listOf("americanexpress.com")
        )
    }
}
