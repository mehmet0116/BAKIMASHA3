package com.assanhanil.techassist.domain.repository

import com.assanhanil.techassist.domain.model.Bearing
import com.assanhanil.techassist.domain.model.BearingSearchResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for bearing data operations.
 */
interface BearingRepository {
    
    /**
     * Find bearings by their dimensions with tolerance.
     */
    suspend fun findByDimensions(
        innerDiameter: Double,
        outerDiameter: Double,
        width: Double,
        tolerance: Double = 0.5
    ): BearingSearchResult

    /**
     * Find a bearing by its ISO code.
     */
    suspend fun findByIsoCode(isoCode: String): Bearing?

    /**
     * Get all bearings from the database.
     */
    fun getAllBearings(): Flow<List<Bearing>>

    /**
     * Insert a bearing into the database.
     */
    suspend fun insertBearing(bearing: Bearing): Long

    /**
     * Insert multiple bearings into the database.
     */
    suspend fun insertBearings(bearings: List<Bearing>)
}
