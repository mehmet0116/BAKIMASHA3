package com.assanhanil.techassist.domain.repository

import com.assanhanil.techassist.domain.model.ExcelTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Excel template data operations.
 * Allows users to save and load custom Excel templates.
 */
interface ExcelTemplateRepository {
    
    /**
     * Get all active templates.
     */
    fun getAllTemplates(): Flow<List<ExcelTemplate>>

    /**
     * Search templates by name.
     */
    fun searchTemplates(query: String): Flow<List<ExcelTemplate>>

    /**
     * Get a template by ID.
     */
    suspend fun getTemplateById(id: Long): ExcelTemplate?

    /**
     * Get a template by name.
     */
    suspend fun getTemplateByName(name: String): ExcelTemplate?

    /**
     * Save/update a template.
     */
    suspend fun saveTemplate(template: ExcelTemplate): Long

    /**
     * Delete a template.
     */
    suspend fun deleteTemplate(template: ExcelTemplate)

    /**
     * Deactivate a template (soft delete).
     */
    suspend fun deactivateTemplate(templateId: Long)
}
