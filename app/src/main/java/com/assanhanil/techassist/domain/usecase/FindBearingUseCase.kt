package com.assanhanil.techassist.domain.usecase

import com.assanhanil.techassist.domain.model.Bearing
import com.assanhanil.techassist.domain.model.BearingSearchResult
import com.assanhanil.techassist.domain.repository.BearingRepository

/**
 * Use case for finding bearings by their measured dimensions.
 * Part of the Visual Bearing Finder feature.
 */
class FindBearingUseCase(
    private val bearingRepository: BearingRepository
) {
    /**
     * Find bearings that match the given dimensions within a tolerance.
     *
     * @param innerDiameter The measured inner diameter (ID) in mm
     * @param outerDiameter The measured outer diameter (OD) in mm
     * @param width The measured width in mm
     * @param tolerance The acceptable tolerance in mm (default 0.5mm)
     * @return BearingSearchResult containing found bearings or error
     */
    suspend operator fun invoke(
        innerDiameter: Double,
        outerDiameter: Double,
        width: Double,
        tolerance: Double = 0.5
    ): BearingSearchResult {
        return try {
            // Validate input dimensions
            if (innerDiameter <= 0 || outerDiameter <= 0 || width <= 0) {
                return BearingSearchResult.Error(
                    IllegalArgumentException("All dimensions must be positive values")
                )
            }
            
            if (innerDiameter >= outerDiameter) {
                return BearingSearchResult.Error(
                    IllegalArgumentException("Inner diameter must be less than outer diameter")
                )
            }

            bearingRepository.findByDimensions(
                innerDiameter = innerDiameter,
                outerDiameter = outerDiameter,
                width = width,
                tolerance = tolerance
            )
        } catch (e: Exception) {
            BearingSearchResult.Error(e)
        }
    }
}
