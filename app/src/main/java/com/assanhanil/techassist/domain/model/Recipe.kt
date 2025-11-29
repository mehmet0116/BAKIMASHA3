package com.assanhanil.techassist.domain.model

import java.util.Date

/**
 * Domain model representing a Master Recipe.
 * Admins can save filled forms as "Master Recipes" for operators to load instantly.
 */
data class Recipe(
    val id: Long = 0,
    val name: String,                    // e.g., "Monthly Press Maintenance"
    val description: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val createdBy: String = "",
    val sections: List<RecipeSection> = emptyList(),
    val isActive: Boolean = true
)

/**
 * A section within a recipe template.
 */
data class RecipeSection(
    val id: Long = 0,
    val title: String,
    val order: Int = 0,
    val fields: List<RecipeField> = emptyList()
)

/**
 * A field template within a recipe section.
 */
data class RecipeField(
    val id: Long = 0,
    val label: String,
    val fieldType: FieldType,
    val defaultValue: String = "",
    val isRequired: Boolean = false,
    val order: Int = 0
)
