package com.assanhanil.techassist.domain.model

/**
 * Domain model for an operator.
 * Represents a person who performs maintenance controls.
 */
data class Operator(
    val id: Long = 0,
    val name: String,
    val department: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
