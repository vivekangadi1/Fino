package com.fino.app.service.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.fino.app.domain.model.ExportFormat
import com.fino.app.domain.model.ExportRequest
import com.fino.app.domain.model.ExportResult
import com.fino.app.domain.model.Transaction
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Exports transaction data to CSV format.
 */
class CsvExporter @Inject constructor(
    private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /**
     * Export transactions to CSV file.
     *
     * @param request Export request containing transactions and date range
     * @return ExportResult with file URI or error
     */
    suspend fun export(request: ExportRequest): ExportResult = withContext(Dispatchers.IO) {
        try {
            // Generate file name
            val fileName = generateFileName(request)

            // Create file in cache directory (for sharing)
            val file = File(context.cacheDir, fileName)

            // Prepare CSV data
            val rows = mutableListOf<List<String>>()

            // Add header row
            rows.add(listOf(
                "Date",
                "Merchant",
                "Amount (INR)",
                "Type",
                "Payment Method",
                "Reference",
                "Bank",
                "Card Last 4",
                "Source"
            ))

            // Add transaction rows
            request.transactions.forEach { transaction ->
                rows.add(listOf(
                    transaction.transactionDate.format(dateTimeFormatter),
                    escapeCsvValue(transaction.merchantNormalized ?: transaction.merchantName),
                    String.format("%.2f", transaction.amount),
                    transaction.type.name,
                    transaction.paymentMethod ?: "",
                    transaction.reference ?: "",
                    transaction.bankName ?: "",
                    transaction.cardLastFour ?: "",
                    transaction.source.name
                ))
            }

            // Write CSV file
            csvWriter().writeAll(rows, file)

            // Get URI using FileProvider
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            ExportResult.Success(
                fileUri = fileUri,
                fileName = fileName,
                format = ExportFormat.CSV
            )
        } catch (e: Exception) {
            ExportResult.Error(
                error = "Failed to export CSV: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Generate file name for export.
     * Format: fino_transactions_YYYY-MM-DD_to_YYYY-MM-DD.csv
     */
    private fun generateFileName(request: ExportRequest): String {
        val startDateStr = request.startDate.format(dateFormatter)
        val endDateStr = request.endDate.format(dateFormatter)
        return "fino_transactions_${startDateStr}_to_${endDateStr}.csv"
    }

    /**
     * Escape special characters in CSV values.
     */
    private fun escapeCsvValue(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
