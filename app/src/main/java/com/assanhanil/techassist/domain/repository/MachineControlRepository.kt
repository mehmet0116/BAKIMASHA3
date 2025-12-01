package com.assanhanil.techassist.domain.repository

import com.assanhanil.techassist.domain.model.MachineControl
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for machine control data operations.
 * Allows users to save and load machine control configurations.
 */
interface MachineControlRepository {
    
    /**
     * Get all active machine controls.
     */
    fun getAllMachineControls(): Flow<List<MachineControl>>

    /**
     * Search machine controls by title.
     */
    fun searchMachineControls(query: String): Flow<List<MachineControl>>

    /**
     * Get a machine control by ID.
     */
    suspend fun getMachineControlById(id: Long): MachineControl?

    /**
     * Get a machine control by title.
     */
    suspend fun getMachineControlByTitle(title: String): MachineControl?

    /**
     * Save/update a machine control.
     */
    suspend fun saveMachineControl(machineControl: MachineControl): Long

    /**
     * Delete a machine control.
     */
    suspend fun deleteMachineControl(machineControl: MachineControl)

    /**
     * Deactivate a machine control (soft delete).
     */
    suspend fun deactivateMachineControl(machineControlId: Long)
}
