package com.assanhanil.techassist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing Recipe templates.
 * Admins save filled forms as "Master Recipes" for operators to load instantly.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String = "",
    val sectionsJson: String = "[]",  // JSON serialized sections
    val isActive: Boolean = true
)
