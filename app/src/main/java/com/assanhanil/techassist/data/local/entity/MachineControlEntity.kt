package com.assanhanil.techassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing machine control data.
 * Users can create machines with titles and save control photos/notes.
 */
@Entity(tableName = "machine_controls")
data class MachineControlEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val controlItemsJson: String = "[]",  // JSON serialized list of control items
    val operatorIdsJson: String = "[]",   // JSON serialized list of operator IDs
    val isActive: Boolean = true
)
