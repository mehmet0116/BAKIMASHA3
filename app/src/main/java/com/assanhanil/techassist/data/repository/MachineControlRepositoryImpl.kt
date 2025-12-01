package com.assanhanil.techassist.data.repository

import com.assanhanil.techassist.data.local.dao.MachineControlDao
import com.assanhanil.techassist.data.local.entity.MachineControlEntity
import com.assanhanil.techassist.domain.model.ControlItemData
import com.assanhanil.techassist.domain.model.MachineControl
import com.assanhanil.techassist.domain.model.SecurityStatus
import com.assanhanil.techassist.domain.repository.MachineControlRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * Implementation of MachineControlRepository using Room database.
 * Provides offline-first machine control storage functionality.
 */
class MachineControlRepositoryImpl(
    private val machineControlDao: MachineControlDao
) : MachineControlRepository {

    companion object {
        private const val EMPTY_JSON_ARRAY = "[]"
    }

    override fun getAllMachineControls(): Flow<List<MachineControl>> {
        return machineControlDao.getAllMachineControls().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchMachineControls(query: String): Flow<List<MachineControl>> {
        return machineControlDao.searchMachineControls(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMachineControlById(id: Long): MachineControl? {
        return machineControlDao.getMachineControlById(id)?.toDomain()
    }

    override suspend fun getMachineControlByTitle(title: String): MachineControl? {
        return machineControlDao.getMachineControlByTitle(title)?.toDomain()
    }

    override suspend fun saveMachineControl(machineControl: MachineControl): Long {
        return machineControlDao.insertMachineControl(machineControl.toEntity())
    }

    override suspend fun deleteMachineControl(machineControl: MachineControl) {
        machineControlDao.deleteMachineControl(machineControl.toEntity())
    }

    override suspend fun deactivateMachineControl(machineControlId: Long) {
        machineControlDao.deactivateMachineControl(machineControlId, System.currentTimeMillis())
    }

    // Extension functions for mapping between domain and entity
    private fun MachineControlEntity.toDomain(): MachineControl {
        return MachineControl(
            id = id,
            title = title,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            controlItems = parseControlItemsJson(controlItemsJson),
            operatorIds = parseOperatorIdsJson(operatorIdsJson),
            isActive = isActive
        )
    }

    private fun MachineControl.toEntity(): MachineControlEntity {
        return MachineControlEntity(
            id = id,
            title = title,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            controlItemsJson = controlItemsToJson(controlItems),
            operatorIdsJson = operatorIdsToJson(operatorIds),
            isActive = isActive
        )
    }

    /**
     * Parse JSON string to list of ControlItemData.
     */
    private fun parseControlItemsJson(json: String): List<ControlItemData> {
        if (json.isBlank() || json == EMPTY_JSON_ARRAY) return emptyList()
        
        return try {
            val jsonArray = JSONArray(json)
            val items = mutableListOf<ControlItemData>()
            
            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.getJSONObject(i)
                items.add(
                    ControlItemData(
                        id = itemObj.optInt("id", 0),
                        title = itemObj.optString("title", ""),
                        notes = itemObj.optString("notes", ""),
                        imagePath = itemObj.optString("imagePath", ""),
                        timestamp = itemObj.optLong("timestamp", 0),
                        status = itemObj.optString("status", ""),
                        securityStatus = parseSecurityStatus(itemObj.optString("securityStatus", "NOT_SET"))
                    )
                )
            }
            
            items
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert list of ControlItemData to JSON string.
     */
    private fun controlItemsToJson(items: List<ControlItemData>): String {
        val jsonArray = JSONArray()
        
        items.forEach { item ->
            jsonArray.put(JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("notes", item.notes)
                put("imagePath", item.imagePath)
                put("timestamp", item.timestamp)
                put("status", item.status)
                put("securityStatus", item.securityStatus.name)
            })
        }
        
        return jsonArray.toString()
    }

    /**
     * Parse JSON string to list of operator IDs.
     */
    private fun parseOperatorIdsJson(json: String): List<Long> {
        if (json.isBlank() || json == EMPTY_JSON_ARRAY) return emptyList()
        
        return try {
            val jsonArray = JSONArray(json)
            val ids = mutableListOf<Long>()
            
            for (i in 0 until jsonArray.length()) {
                ids.add(jsonArray.getLong(i))
            }
            
            ids
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert list of operator IDs to JSON string.
     */
    private fun operatorIdsToJson(ids: List<Long>): String {
        val jsonArray = JSONArray()
        ids.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    private fun parseSecurityStatus(status: String): SecurityStatus {
        return try {
            SecurityStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            SecurityStatus.NOT_SET
        }
    }
}
