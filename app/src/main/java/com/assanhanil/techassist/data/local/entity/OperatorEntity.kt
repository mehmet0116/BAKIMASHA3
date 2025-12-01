package com.assanhanil.techassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing operator data.
 * Operators can be added in settings and assigned to machine controls.
 */
@Entity(tableName = "operators")
data class OperatorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val department: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
