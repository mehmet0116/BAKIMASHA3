package com.assanhanil.techassist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.assanhanil.techassist.data.local.entity.ExcelTemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Excel template operations.
 * Allows users to save and load custom Excel templates.
 */
@Dao
interface ExcelTemplateDao {

    /**
     * Get all active templates.
     */
    @Query("SELECT * FROM excel_templates WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllTemplates(): Flow<List<ExcelTemplateEntity>>

    /**
     * Search templates by name.
     */
    @Query("SELECT * FROM excel_templates WHERE isActive = 1 AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchTemplates(query: String): Flow<List<ExcelTemplateEntity>>

    /**
     * Get a template by ID.
     */
    @Query("SELECT * FROM excel_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): ExcelTemplateEntity?

    /**
     * Get a template by name.
     */
    @Query("SELECT * FROM excel_templates WHERE name = :name AND isActive = 1 LIMIT 1")
    suspend fun getTemplateByName(name: String): ExcelTemplateEntity?

    /**
     * Insert a new template (returns the generated ID).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ExcelTemplateEntity): Long

    /**
     * Update an existing template.
     */
    @Update
    suspend fun updateTemplate(template: ExcelTemplateEntity)

    /**
     * Delete a template.
     */
    @Delete
    suspend fun deleteTemplate(template: ExcelTemplateEntity)

    /**
     * Soft delete (deactivate) a template.
     */
    @Query("UPDATE excel_templates SET isActive = 0, updatedAt = :timestamp WHERE id = :templateId")
    suspend fun deactivateTemplate(templateId: Long, timestamp: Long)

    /**
     * Get count of templates.
     */
    @Query("SELECT COUNT(*) FROM excel_templates WHERE isActive = 1")
    suspend fun getCount(): Int
}
