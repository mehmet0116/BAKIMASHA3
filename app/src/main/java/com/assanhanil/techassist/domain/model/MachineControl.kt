package com.assanhanil.techassist.domain.model

/**
 * Domain model for machine control.
 * Represents a machine that can be controlled and inspected.
 */
data class MachineControl(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val controlItems: List<ControlItemData> = emptyList(),
    val operatorIds: List<Long> = emptyList(),
    val isActive: Boolean = true
)

/**
 * Data class representing a control item with photo and metadata.
 * Used for JSON serialization.
 */
data class ControlItemData(
    val id: Int,
    val title: String,
    val notes: String,
    val imagePath: String,
    val timestamp: Long,
    val status: String,
    val securityStatus: SecurityStatus = SecurityStatus.NOT_SET
)

/**
 * Enum for security control status.
 */
enum class SecurityStatus {
    NOT_SET,
    ACTIVE,     // Güvenlik Devrede Aktif
    INACTIVE    // Devrede Değil
}
