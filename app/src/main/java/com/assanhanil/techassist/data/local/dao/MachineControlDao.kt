package com.assanhanil.techassist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.assanhanil.techassist.data.local.entity.MachineControlEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for machine control operations.
 * Allows users to save and load machine control configurations.
 */
@Dao
interface MachineControlDao {

    /**
     * Get all active machine controls.
     */
    @Query("SELECT * FROM machine_controls WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllMachineControls(): Flow<List<MachineControlEntity>>

    /**
     * Search machine controls by title.
     */
    @Query("SELECT * FROM machine_controls WHERE isActive = 1 AND title LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchMachineControls(query: String): Flow<List<MachineControlEntity>>

    /**
     * Get a machine control by ID.
     */
    @Query("SELECT * FROM machine_controls WHERE id = :id")
    suspend fun getMachineControlById(id: Long): MachineControlEntity?

    /**
     * Get a machine control by title.
     */
    @Query("SELECT * FROM machine_controls WHERE title = :title AND isActive = 1 LIMIT 1")
    suspend fun getMachineControlByTitle(title: String): MachineControlEntity?

    /**
     * Insert a new machine control (returns the generated ID).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachineControl(machineControl: MachineControlEntity): Long

    /**
     * Update an existing machine control.
     */
    @Update
    suspend fun updateMachineControl(machineControl: MachineControlEntity)

    /**
     * Delete a machine control.
     */
    @Delete
    suspend fun deleteMachineControl(machineControl: MachineControlEntity)

    /**
     * Soft delete (deactivate) a machine control.
     */
    @Query("UPDATE machine_controls SET isActive = 0, updatedAt = :timestamp WHERE id = :machineControlId")
    suspend fun deactivateMachineControl(machineControlId: Long, timestamp: Long)

    /**
     * Get count of machine controls.
     */
    @Query("SELECT COUNT(*) FROM machine_controls WHERE isActive = 1")
    suspend fun getCount(): Int
}
