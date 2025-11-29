package com.assanhanil.techassist.domain.model

/**
 * Domain model representing a bearing with its specifications.
 * Used for the Visual Bearing Finder feature.
 */
data class Bearing(
    val id: Long = 0,
    val isoCode: String,        // e.g., "6204-ZZ"
    val innerDiameter: Double,  // ID in mm
    val outerDiameter: Double,  // OD in mm
    val width: Double,          // Width in mm
    val type: String,           // e.g., "Deep Groove Ball Bearing"
    val sealType: String = "",  // e.g., "ZZ", "2RS", "Open"
    val description: String = ""
)

/**
 * Result of a bearing search query.
 */
sealed class BearingSearchResult {
    data class Found(val bearings: List<Bearing>) : BearingSearchResult()
    data class NotFound(val message: String) : BearingSearchResult()
    data class Error(val exception: Throwable) : BearingSearchResult()
}
