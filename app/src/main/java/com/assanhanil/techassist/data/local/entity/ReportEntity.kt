package com.assanhanil.techassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing maintenance report drafts.
 * Autosave: Every keystroke saved to Room DB (Drafts). No data loss on crash/battery die.
 */
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val operatorName: String = "",
    val description: String = "",
    val sectionsJson: String = "[]",  // JSON serialized sections
    val signatureImagePath: String? = null,
    val isDraft: Boolean = true
)
