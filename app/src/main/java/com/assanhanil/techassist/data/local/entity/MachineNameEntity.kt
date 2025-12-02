package com.assanhanil.techassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing machine names (templates).
 * This is a lightweight entity that only stores machine names,
 * separate from control data. Control data is exported to Excel
 * and not persisted after export.
 */
@Entity(tableName = "machine_names")
data class MachineNameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
