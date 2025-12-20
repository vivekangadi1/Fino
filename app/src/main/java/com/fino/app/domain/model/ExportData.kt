package com.fino.app.domain.model

import android.net.Uri
import java.time.LocalDate

/**
 * Supported export file formats.
 */
enum class ExportFormat {
    CSV,
    PDF
}

/**
 * Request to export transaction data.
 *
 * @property format Desired export format (CSV or PDF)
 * @property startDate Start date of the period to export (inclusive)
 * @property endDate End date of the period to export (inclusive)
 * @property transactions List of transactions to export
 */
data class ExportRequest(
    val format: ExportFormat,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val transactions: List<Transaction>
)

/**
 * Result of an export operation.
 */
sealed class ExportResult {
    /**
     * Export succeeded.
     *
     * @property fileUri URI to the exported file
     * @property fileName Name of the exported file
     * @property format Format of the export
     */
    data class Success(
        val fileUri: Uri,
        val fileName: String,
        val format: ExportFormat
    ) : ExportResult()

    /**
     * Export failed.
     *
     * @property error Error message describing the failure
     * @property exception Optional exception that caused the failure
     */
    data class Error(
        val error: String,
        val exception: Throwable? = null
    ) : ExportResult()
}
