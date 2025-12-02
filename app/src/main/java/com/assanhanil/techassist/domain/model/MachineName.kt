package com.assanhanil.techassist.domain.model

/**
 * Domain model for machine name (template).
 * Represents a machine name that can be quickly selected for new inspections.
 * Control data is not persisted with this model - it's exported to Excel.
 */
data class MachineName(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
