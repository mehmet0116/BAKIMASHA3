package com.assanhanil.techassist.domain.model

/**
 * Domain model for Excel template.
 * Allows users to create custom Excel templates with their own column/row structure.
 */
data class ExcelTemplate(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val columns: List<TemplateColumn> = emptyList(),
    val rows: List<TemplateRow> = emptyList(),
    val headerRowCount: Int = 1
)

/**
 * Represents a column in the Excel template.
 */
data class TemplateColumn(
    val id: Int,
    val name: String,
    val width: Int = 15,  // Excel column width in characters
    val dataType: ColumnDataType = ColumnDataType.TEXT,
    val isRequired: Boolean = false,
    val defaultValue: String = ""
)

/**
 * Represents a row in the Excel template.
 */
data class TemplateRow(
    val id: Int,
    val rowIndex: Int,
    val isHeader: Boolean = false,
    val height: Float = 20f,  // Row height in points
    val cells: List<TemplateCell> = emptyList()
)

/**
 * Represents a cell in the Excel template.
 */
data class TemplateCell(
    val columnId: Int,
    val value: String = "",
    val isEditable: Boolean = true,
    val style: CellStyle = CellStyle()
)

/**
 * Cell styling options.
 */
data class CellStyle(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val fontSize: Int = 11,
    val alignment: CellAlignment = CellAlignment.LEFT
)

/**
 * Cell text alignment options.
 */
enum class CellAlignment {
    LEFT, CENTER, RIGHT
}

/**
 * Column data types for validation.
 */
enum class ColumnDataType {
    TEXT,
    NUMBER,
    DATE,
    BOOLEAN,
    IMAGE
}
