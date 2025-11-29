package com.assanhanil.techassist.domain.repository

import com.assanhanil.techassist.domain.model.MaintenanceReport
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for maintenance report data operations.
 */
interface ReportRepository {
    
    /**
     * Get all reports.
     */
    fun getAllReports(): Flow<List<MaintenanceReport>>

    /**
     * Get all draft reports (autosaved).
     */
    fun getDraftReports(): Flow<List<MaintenanceReport>>

    /**
     * Get a report by ID.
     */
    suspend fun getReportById(id: Long): MaintenanceReport?

    /**
     * Save/update a report (autosave on every keystroke).
     */
    suspend fun saveReport(report: MaintenanceReport): Long

    /**
     * Delete a report.
     */
    suspend fun deleteReport(report: MaintenanceReport)

    /**
     * Mark a report as finalized (no longer a draft).
     */
    suspend fun finalizeReport(reportId: Long)
}
