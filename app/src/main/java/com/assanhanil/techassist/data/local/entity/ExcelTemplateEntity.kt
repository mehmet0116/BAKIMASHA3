package com.assanhanil.techassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing custom Excel templates.
 * Users can create their own Excel templates with custom columns and rows.
 */
@Entity(tableName = "excel_templates")
data class ExcelTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val columnsJson: String = "[]",  // JSON serialized list of TemplateColumn
    val rowsJson: String = "[]",     // JSON serialized list of TemplateRow
    val headerRowCount: Int = 1,
    val isActive: Boolean = true
)
