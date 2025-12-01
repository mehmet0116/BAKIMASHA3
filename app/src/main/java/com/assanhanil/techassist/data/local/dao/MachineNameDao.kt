package com.assanhanil.techassist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.assanhanil.techassist.data.local.entity.MachineNameEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for machine name operations.
 * Machine names are templates that persist for quick selection.
 * Control data is exported to Excel and not saved with these names.
 */
@Dao
interface MachineNameDao {

    /**
     * Get all machine names ordered by creation time (newest first).
     */
    @Query("SELECT * FROM machine_names ORDER BY createdAt DESC")
    fun getAllMachineNames(): Flow<List<MachineNameEntity>>

    /**
     * Get a machine name by ID.
     */
    @Query("SELECT * FROM machine_names WHERE id = :id")
    suspend fun getMachineNameById(id: Long): MachineNameEntity?

    /**
     * Get a machine name by name (case-insensitive).
     */
    @Query("SELECT * FROM machine_names WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getMachineNameByName(name: String): MachineNameEntity?

    /**
     * Insert a new machine name (returns the generated ID).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMachineName(machineName: MachineNameEntity): Long

    /**
     * Delete a machine name.
     */
    @Delete
    suspend fun deleteMachineName(machineName: MachineNameEntity)

    /**
     * Delete a machine name by ID.
     */
    @Query("DELETE FROM machine_names WHERE id = :id")
    suspend fun deleteMachineNameById(id: Long)

    /**
     * Get count of machine names.
     */
    @Query("SELECT COUNT(*) FROM machine_names")
    suspend fun getCount(): Int
}
