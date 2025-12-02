package com.assanhanil.techassist.domain.repository

import com.assanhanil.techassist.domain.model.MachineName
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for machine name operations.
 * Machine names are templates that persist for quick selection.
 */
interface MachineNameRepository {
    
    /**
     * Get all machine names.
     */
    fun getAllMachineNames(): Flow<List<MachineName>>
    
    /**
     * Get a machine name by ID.
     */
    suspend fun getMachineNameById(id: Long): MachineName?
    
    /**
     * Get a machine name by name.
     */
    suspend fun getMachineNameByName(name: String): MachineName?
    
    /**
     * Save a new machine name.
     * Returns the ID of the saved machine name.
     */
    suspend fun saveMachineName(name: String): Long
    
    /**
     * Delete a machine name by ID.
     */
    suspend fun deleteMachineName(id: Long)
}
