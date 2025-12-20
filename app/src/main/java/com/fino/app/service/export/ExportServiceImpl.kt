package com.fino.app.service.export

import com.fino.app.domain.model.ExportFormat
import com.fino.app.domain.model.ExportRequest
import com.fino.app.domain.model.ExportResult
import javax.inject.Inject

/**
 * Implementation of ExportService that delegates to format-specific exporters.
 */
class ExportServiceImpl @Inject constructor(
    private val csvExporter: CsvExporter,
    private val pdfExporter: PdfExporter
) : ExportService {

    override suspend fun exportTransactions(request: ExportRequest): ExportResult {
        return when (request.format) {
            ExportFormat.CSV -> csvExporter.export(request)
            ExportFormat.PDF -> pdfExporter.export(request)
        }
    }
}
