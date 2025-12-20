package com.fino.app.service.export

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider
import com.fino.app.domain.model.ExportFormat
import com.fino.app.domain.model.ExportRequest
import com.fino.app.domain.model.ExportResult
import com.fino.app.domain.model.TransactionType
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Exports transaction data to PDF format.
 */
class PdfExporter @Inject constructor(
    private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    companion object {
        private const val MARGIN = 50f
        private const val FONT_SIZE_TITLE = 18f
        private const val FONT_SIZE_HEADING = 12f
        private const val FONT_SIZE_BODY = 10f
        private const val LINE_HEIGHT = 15f
        private const val SECTION_SPACING = 20f
    }

    /**
     * Export transactions to PDF file.
     *
     * @param request Export request containing transactions and date range
     * @return ExportResult with file URI or error
     */
    suspend fun export(request: ExportRequest): ExportResult = withContext(Dispatchers.IO) {
        try {
            // Initialize PDFBox for Android
            com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context)

            // Generate file name
            val fileName = generateFileName(request)

            // Create file in cache directory (for sharing)
            val file = File(context.cacheDir, fileName)

            // Create PDF document
            val document = PDDocument()

            try {
                // Add pages with content
                addContentToDocument(document, request)

                // Save document
                document.save(file)

                // Get URI using FileProvider
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                ExportResult.Success(
                    fileUri = fileUri,
                    fileName = fileName,
                    format = ExportFormat.PDF
                )
            } finally {
                document.close()
            }
        } catch (e: Exception) {
            ExportResult.Error(
                error = "Failed to export PDF: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Add content to PDF document.
     */
    private fun addContentToDocument(document: PDDocument, request: ExportRequest) {
        var page = PDPage(PDRectangle.A4)
        document.addPage(page)

        var contentStream = PDPageContentStream(document, page)
        var yPosition = PDRectangle.A4.height - MARGIN

        try {
            // Title
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TITLE)
            contentStream.newLineAtOffset(MARGIN, yPosition)
            contentStream.showText("Transaction Report")
            contentStream.endText()
            yPosition -= SECTION_SPACING * 2

            // Date range
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_BODY)
            contentStream.newLineAtOffset(MARGIN, yPosition)
            val dateRange = "${request.startDate.format(dateFormatter)} to ${request.endDate.format(dateFormatter)}"
            contentStream.showText("Period: $dateRange")
            contentStream.endText()
            yPosition -= SECTION_SPACING

            // Calculate summary statistics
            val totalIncome = request.transactions
                .filter { it.type == TransactionType.CREDIT }
                .sumOf { it.amount }

            val totalExpenses = request.transactions
                .filter { it.type == TransactionType.DEBIT }
                .sumOf { it.amount }

            val netBalance = totalIncome - totalExpenses

            // Summary section
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_HEADING)
            contentStream.newLineAtOffset(MARGIN, yPosition)
            contentStream.showText("Summary")
            contentStream.endText()
            yPosition -= LINE_HEIGHT * 1.5f

            // Summary statistics
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_BODY)
            contentStream.newLineAtOffset(MARGIN, yPosition)
            contentStream.showText("Total Income: Rs. ${String.format("%.2f", totalIncome)}")
            contentStream.endText()
            yPosition -= LINE_HEIGHT

            contentStream.beginText()
            contentStream.newLineAtOffset(MARGIN, yPosition)
            contentStream.showText("Total Expenses: Rs. ${String.format("%.2f", totalExpenses)}")
            contentStream.endText()
            yPosition -= LINE_HEIGHT

            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_BODY)
            contentStream.newLineAtOffset(MARGIN, yPosition)
            contentStream.showText("Net Balance: Rs. ${String.format("%.2f", netBalance)}")
            contentStream.endText()
            yPosition -= SECTION_SPACING

            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_BODY)
            contentStream.newLineAtOffset(MARGIN, yPosition)
            contentStream.showText("Transaction Count: ${request.transactions.size}")
            contentStream.endText()
            yPosition -= SECTION_SPACING * 1.5f

            // Transactions section
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_HEADING)
            contentStream.newLineAtOffset(MARGIN, yPosition)
            contentStream.showText("Transactions")
            contentStream.endText()
            yPosition -= LINE_HEIGHT * 1.5f

            contentStream.close()

            // Add transactions (may span multiple pages)
            addTransactions(document, request.transactions, yPosition)
        } catch (e: Exception) {
            contentStream.close()
            throw e
        }
    }

    /**
     * Add transactions to PDF, handling page breaks.
     */
    private fun addTransactions(
        document: PDDocument,
        transactions: List<com.fino.app.domain.model.Transaction>,
        startY: Float
    ) {
        var currentPage = document.getPage(document.numberOfPages - 1)
        var contentStream = PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true)
        var yPosition = startY

        try {
            for (transaction in transactions) {
                // Check if we need a new page
                if (yPosition < MARGIN + LINE_HEIGHT * 3) {
                    contentStream.close()
                    currentPage = PDPage(PDRectangle.A4)
                    document.addPage(currentPage)
                    contentStream = PDPageContentStream(document, currentPage)
                    yPosition = PDRectangle.A4.height - MARGIN
                }

                // Transaction date and merchant
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_BODY)
                contentStream.newLineAtOffset(MARGIN, yPosition)
                val merchantName = transaction.merchantNormalized ?: transaction.merchantName
                val truncatedMerchant = if (merchantName.length > 40) {
                    merchantName.take(37) + "..."
                } else {
                    merchantName
                }
                contentStream.showText(truncatedMerchant)
                contentStream.endText()
                yPosition -= LINE_HEIGHT

                // Transaction details
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_BODY)
                contentStream.newLineAtOffset(MARGIN, yPosition)
                val dateStr = transaction.transactionDate.format(dateTimeFormatter)
                val amountStr = String.format("%.2f", transaction.amount)
                val typeStr = if (transaction.type == TransactionType.CREDIT) "+" else "-"
                contentStream.showText("$dateStr | $typeStr Rs. $amountStr | ${transaction.type.name}")
                contentStream.endText()
                yPosition -= LINE_HEIGHT

                // Payment method if available
                if (transaction.paymentMethod != null) {
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_BODY - 1)
                    contentStream.newLineAtOffset(MARGIN, yPosition)
                    contentStream.showText("Payment: ${transaction.paymentMethod}")
                    if (transaction.cardLastFour != null) {
                        contentStream.showText(" (*${transaction.cardLastFour})")
                    }
                    contentStream.endText()
                    yPosition -= LINE_HEIGHT
                }

                yPosition -= LINE_HEIGHT * 0.5f // Extra spacing between transactions
            }
        } finally {
            contentStream.close()
        }
    }

    /**
     * Generate file name for export.
     * Format: fino_transactions_YYYY-MM-DD_to_YYYY-MM-DD.pdf
     */
    private fun generateFileName(request: ExportRequest): String {
        val startDateStr = request.startDate.format(dateFormatter)
        val endDateStr = request.endDate.format(dateFormatter)
        return "fino_transactions_${startDateStr}_to_${endDateStr}.pdf"
    }
}
