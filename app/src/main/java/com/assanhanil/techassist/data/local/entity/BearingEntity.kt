package com.assanhanil.techassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing bearing data in the offline database.
 */
@Entity(tableName = "bearings")
data class BearingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val isoCode: String,
    val innerDiameter: Double,
    val outerDiameter: Double,
    val width: Double,
    val type: String,
    val sealType: String = "",
    val description: String = ""
)
