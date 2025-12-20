package com.fino.app.service.export

import com.fino.app.domain.model.ExportRequest
import com.fino.app.domain.model.ExportResult

/**
 * Service for exporting transaction data to various formats.
 */
interface ExportService {
    /**
     * Export transactions according to the provided request.
     *
     * @param request Export request containing format, date range, and transactions
     * @return ExportResult.Success with file URI or ExportResult.Error on failure
     */
    suspend fun exportTransactions(request: ExportRequest): ExportResult
}
