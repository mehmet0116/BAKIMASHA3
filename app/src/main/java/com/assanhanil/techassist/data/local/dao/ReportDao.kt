package com.assanhanil.techassist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.assanhanil.techassist.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for report operations.
 * Supports autosave functionality - every keystroke saved to Room DB.
 */
@Dao
interface ReportDao {

    /**
     * Get all reports.
     */
    @Query("SELECT * FROM reports ORDER BY updatedAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    /**
     * Get all draft reports.
     */
    @Query("SELECT * FROM reports WHERE isDraft = 1 ORDER BY updatedAt DESC")
    fun getDraftReports(): Flow<List<ReportEntity>>

    /**
     * Get completed (finalized) reports.
     */
    @Query("SELECT * FROM reports WHERE isDraft = 0 ORDER BY updatedAt DESC")
    fun getCompletedReports(): Flow<List<ReportEntity>>

    /**
     * Get a report by ID.
     */
    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReportById(id: Long): ReportEntity?

    /**
     * Insert a new report (returns the generated ID).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    /**
     * Update an existing report.
     */
    @Update
    suspend fun updateReport(report: ReportEntity)

    /**
     * Delete a report.
     */
    @Delete
    suspend fun deleteReport(report: ReportEntity)

    /**
     * Mark a report as finalized (no longer a draft).
     */
    @Query("UPDATE reports SET isDraft = 0, updatedAt = :timestamp WHERE id = :reportId")
    suspend fun finalizeReport(reportId: Long, timestamp: Long)
}
