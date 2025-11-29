package com.assanhanil.techassist.domain.model

import java.util.Date

/**
 * Domain model representing a maintenance report.
 */
data class MaintenanceReport(
    val id: Long = 0,
    val title: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val operatorName: String = "",
    val description: String = "",
    val sections: List<ReportSection> = emptyList(),
    val signatureImagePath: String? = null,
    val isDraft: Boolean = true
)

/**
 * A section within a maintenance report.
 */
data class ReportSection(
    val id: Long = 0,
    val title: String,
    val fields: List<ReportField> = emptyList(),
    val imagePaths: List<String> = emptyList()
)

/**
 * A single field within a report section.
 */
data class ReportField(
    val id: Long = 0,
    val label: String,
    val value: String = "",
    val fieldType: FieldType = FieldType.TEXT,
    val isRequired: Boolean = false
)

/**
 * Types of fields available in a report.
 */
enum class FieldType {
    TEXT,
    NUMBER,
    DATE,
    CHECKBOX,
    SIGNATURE
}
