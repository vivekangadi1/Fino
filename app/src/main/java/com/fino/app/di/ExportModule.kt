package com.fino.app.di

import android.content.Context
import com.fino.app.service.export.CsvExporter
import com.fino.app.service.export.ExportService
import com.fino.app.service.export.ExportServiceImpl
import com.fino.app.service.export.PdfExporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExportModule {

    @Provides
    @Singleton
    fun provideCsvExporter(
        @ApplicationContext context: Context
    ): CsvExporter {
        return CsvExporter(context)
    }

    @Provides
    @Singleton
    fun providePdfExporter(
        @ApplicationContext context: Context
    ): PdfExporter {
        return PdfExporter(context)
    }

    @Provides
    @Singleton
    fun provideExportService(
        csvExporter: CsvExporter,
        pdfExporter: PdfExporter
    ): ExportService {
        return ExportServiceImpl(csvExporter, pdfExporter)
    }
}
