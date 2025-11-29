package com.assanhanil.techassist.data.repository

import com.assanhanil.techassist.data.local.dao.ExcelTemplateDao
import com.assanhanil.techassist.data.local.entity.ExcelTemplateEntity
import com.assanhanil.techassist.domain.model.CellAlignment
import com.assanhanil.techassist.domain.model.CellStyle
import com.assanhanil.techassist.domain.model.ColumnDataType
import com.assanhanil.techassist.domain.model.ExcelTemplate
import com.assanhanil.techassist.domain.model.TemplateCell
import com.assanhanil.techassist.domain.model.TemplateColumn
import com.assanhanil.techassist.domain.model.TemplateRow
import com.assanhanil.techassist.domain.repository.ExcelTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * Implementation of ExcelTemplateRepository using Room database.
 * Provides offline-first Excel template storage functionality.
 */
class ExcelTemplateRepositoryImpl(
    private val excelTemplateDao: ExcelTemplateDao
) : ExcelTemplateRepository {

    override fun getAllTemplates(): Flow<List<ExcelTemplate>> {
        return excelTemplateDao.getAllTemplates().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchTemplates(query: String): Flow<List<ExcelTemplate>> {
        return excelTemplateDao.searchTemplates(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTemplateById(id: Long): ExcelTemplate? {
        return excelTemplateDao.getTemplateById(id)?.toDomain()
    }

    override suspend fun getTemplateByName(name: String): ExcelTemplate? {
        return excelTemplateDao.getTemplateByName(name)?.toDomain()
    }

    override suspend fun saveTemplate(template: ExcelTemplate): Long {
        return excelTemplateDao.insertTemplate(template.toEntity())
    }

    override suspend fun deleteTemplate(template: ExcelTemplate) {
        excelTemplateDao.deleteTemplate(template.toEntity())
    }

    override suspend fun deactivateTemplate(templateId: Long) {
        excelTemplateDao.deactivateTemplate(templateId, System.currentTimeMillis())
    }

    // Extension functions for mapping between domain and entity
    private fun ExcelTemplateEntity.toDomain(): ExcelTemplate {
        return ExcelTemplate(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            columns = parseColumnsJson(columnsJson),
            rows = parseRowsJson(rowsJson),
            headerRowCount = headerRowCount
        )
    }

    private fun ExcelTemplate.toEntity(): ExcelTemplateEntity {
        return ExcelTemplateEntity(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            columnsJson = columnsToJson(columns),
            rowsJson = rowsToJson(rows),
            headerRowCount = headerRowCount,
            isActive = true
        )
    }

    /**
     * Parse JSON string to list of TemplateColumns.
     */
    private fun parseColumnsJson(json: String): List<TemplateColumn> {
        if (json.isBlank() || json == "[]") return emptyList()
        
        return try {
            val jsonArray = JSONArray(json)
            val columns = mutableListOf<TemplateColumn>()
            
            for (i in 0 until jsonArray.length()) {
                val colObj = jsonArray.getJSONObject(i)
                columns.add(
                    TemplateColumn(
                        id = colObj.optInt("id", 0),
                        name = colObj.optString("name", ""),
                        width = colObj.optInt("width", 15),
                        dataType = parseColumnDataType(colObj.optString("dataType", "TEXT")),
                        isRequired = colObj.optBoolean("isRequired", false),
                        defaultValue = colObj.optString("defaultValue", "")
                    )
                )
            }
            
            columns
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert list of TemplateColumns to JSON string.
     */
    private fun columnsToJson(columns: List<TemplateColumn>): String {
        val jsonArray = JSONArray()
        
        columns.forEach { column ->
            jsonArray.put(JSONObject().apply {
                put("id", column.id)
                put("name", column.name)
                put("width", column.width)
                put("dataType", column.dataType.name)
                put("isRequired", column.isRequired)
                put("defaultValue", column.defaultValue)
            })
        }
        
        return jsonArray.toString()
    }

    /**
     * Parse JSON string to list of TemplateRows.
     */
    private fun parseRowsJson(json: String): List<TemplateRow> {
        if (json.isBlank() || json == "[]") return emptyList()
        
        return try {
            val jsonArray = JSONArray(json)
            val rows = mutableListOf<TemplateRow>()
            
            for (i in 0 until jsonArray.length()) {
                val rowObj = jsonArray.getJSONObject(i)
                val cellsArray = rowObj.optJSONArray("cells") ?: JSONArray()
                val cells = mutableListOf<TemplateCell>()
                
                for (j in 0 until cellsArray.length()) {
                    val cellObj = cellsArray.getJSONObject(j)
                    cells.add(
                        TemplateCell(
                            columnId = cellObj.optInt("columnId", 0),
                            value = cellObj.optString("value", ""),
                            isEditable = cellObj.optBoolean("isEditable", true),
                            style = parseCellStyle(cellObj.optJSONObject("style"))
                        )
                    )
                }
                
                rows.add(
                    TemplateRow(
                        id = rowObj.optInt("id", 0),
                        rowIndex = rowObj.optInt("rowIndex", 0),
                        isHeader = rowObj.optBoolean("isHeader", false),
                        height = rowObj.optDouble("height", 20.0).toFloat(),
                        cells = cells
                    )
                )
            }
            
            rows
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert list of TemplateRows to JSON string.
     */
    private fun rowsToJson(rows: List<TemplateRow>): String {
        val jsonArray = JSONArray()
        
        rows.forEach { row ->
            val rowObj = JSONObject().apply {
                put("id", row.id)
                put("rowIndex", row.rowIndex)
                put("isHeader", row.isHeader)
                put("height", row.height.toDouble())
                
                val cellsArray = JSONArray()
                row.cells.forEach { cell ->
                    cellsArray.put(JSONObject().apply {
                        put("columnId", cell.columnId)
                        put("value", cell.value)
                        put("isEditable", cell.isEditable)
                        put("style", cellStyleToJson(cell.style))
                    })
                }
                put("cells", cellsArray)
            }
            jsonArray.put(rowObj)
        }
        
        return jsonArray.toString()
    }

    private fun parseCellStyle(json: JSONObject?): CellStyle {
        if (json == null) return CellStyle()
        
        return CellStyle(
            isBold = json.optBoolean("isBold", false),
            isItalic = json.optBoolean("isItalic", false),
            backgroundColor = json.optString("backgroundColor", null),
            textColor = json.optString("textColor", null),
            fontSize = json.optInt("fontSize", 11),
            alignment = parseCellAlignment(json.optString("alignment", "LEFT"))
        )
    }

    private fun cellStyleToJson(style: CellStyle): JSONObject {
        return JSONObject().apply {
            put("isBold", style.isBold)
            put("isItalic", style.isItalic)
            put("backgroundColor", style.backgroundColor)
            put("textColor", style.textColor)
            put("fontSize", style.fontSize)
            put("alignment", style.alignment.name)
        }
    }

    private fun parseColumnDataType(type: String): ColumnDataType {
        return try {
            ColumnDataType.valueOf(type)
        } catch (e: Exception) {
            ColumnDataType.TEXT
        }
    }

    private fun parseCellAlignment(alignment: String): CellAlignment {
        return try {
            CellAlignment.valueOf(alignment)
        } catch (e: Exception) {
            CellAlignment.LEFT
        }
    }
}
