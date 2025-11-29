package com.assanhanil.techassist.data.repository

import com.assanhanil.techassist.data.local.dao.ReportDao
import com.assanhanil.techassist.data.local.entity.ReportEntity
import com.assanhanil.techassist.domain.model.MaintenanceReport
import com.assanhanil.techassist.domain.model.ReportSection
import com.assanhanil.techassist.domain.model.ReportField
import com.assanhanil.techassist.domain.model.FieldType
import com.assanhanil.techassist.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

/**
 * Implementation of ReportRepository using Room database.
 * Provides offline-first report storage with autosave functionality.
 */
class ReportRepositoryImpl(
    private val reportDao: ReportDao
) : ReportRepository {

    override fun getAllReports(): Flow<List<MaintenanceReport>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDraftReports(): Flow<List<MaintenanceReport>> {
        return reportDao.getDraftReports().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReportById(id: Long): MaintenanceReport? {
        return reportDao.getReportById(id)?.toDomain()
    }

    override suspend fun saveReport(report: MaintenanceReport): Long {
        return reportDao.insertReport(report.toEntity())
    }

    override suspend fun deleteReport(report: MaintenanceReport) {
        reportDao.deleteReport(report.toEntity())
    }

    override suspend fun finalizeReport(reportId: Long) {
        reportDao.finalizeReport(reportId, System.currentTimeMillis())
    }

    // Extension functions for mapping between domain and entity
    private fun ReportEntity.toDomain(): MaintenanceReport {
        return MaintenanceReport(
            id = id,
            title = title,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt),
            operatorName = operatorName,
            description = description,
            sections = parseSectionsJson(sectionsJson),
            signatureImagePath = signatureImagePath,
            isDraft = isDraft
        )
    }

    private fun MaintenanceReport.toEntity(): ReportEntity {
        return ReportEntity(
            id = id,
            title = title,
            createdAt = createdAt.time,
            updatedAt = updatedAt.time,
            operatorName = operatorName,
            description = description,
            sectionsJson = sectionsToJson(sections),
            signatureImagePath = signatureImagePath,
            isDraft = isDraft
        )
    }

    /**
     * Parse JSON string to list of ReportSections.
     */
    private fun parseSectionsJson(json: String): List<ReportSection> {
        if (json.isBlank() || json == "[]") return emptyList()
        
        return try {
            val jsonArray = JSONArray(json)
            val sections = mutableListOf<ReportSection>()
            
            for (i in 0 until jsonArray.length()) {
                val sectionObj = jsonArray.getJSONObject(i)
                val fieldsArray = sectionObj.optJSONArray("fields") ?: JSONArray()
                val imagePathsArray = sectionObj.optJSONArray("imagePaths") ?: JSONArray()
                
                val fields = mutableListOf<ReportField>()
                for (j in 0 until fieldsArray.length()) {
                    val fieldObj = fieldsArray.getJSONObject(j)
                    fields.add(
                        ReportField(
                            id = fieldObj.optLong("id", 0),
                            label = fieldObj.optString("label", ""),
                            value = fieldObj.optString("value", ""),
                            fieldType = parseFieldType(fieldObj.optString("fieldType", "TEXT")),
                            isRequired = fieldObj.optBoolean("isRequired", false)
                        )
                    )
                }
                
                val imagePaths = mutableListOf<String>()
                for (j in 0 until imagePathsArray.length()) {
                    imagePaths.add(imagePathsArray.getString(j))
                }
                
                sections.add(
                    ReportSection(
                        id = sectionObj.optLong("id", 0),
                        title = sectionObj.optString("title", ""),
                        fields = fields,
                        imagePaths = imagePaths
                    )
                )
            }
            
            sections
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert list of ReportSections to JSON string.
     */
    private fun sectionsToJson(sections: List<ReportSection>): String {
        val jsonArray = JSONArray()
        
        sections.forEach { section ->
            val sectionObj = JSONObject().apply {
                put("id", section.id)
                put("title", section.title)
                
                val fieldsArray = JSONArray()
                section.fields.forEach { field ->
                    fieldsArray.put(JSONObject().apply {
                        put("id", field.id)
                        put("label", field.label)
                        put("value", field.value)
                        put("fieldType", field.fieldType.name)
                        put("isRequired", field.isRequired)
                    })
                }
                put("fields", fieldsArray)
                
                val imagePathsArray = JSONArray()
                section.imagePaths.forEach { path ->
                    imagePathsArray.put(path)
                }
                put("imagePaths", imagePathsArray)
            }
            jsonArray.put(sectionObj)
        }
        
        return jsonArray.toString()
    }

    private fun parseFieldType(type: String): FieldType {
        return try {
            FieldType.valueOf(type)
        } catch (e: Exception) {
            FieldType.TEXT
        }
    }
}
