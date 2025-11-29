package com.assanhanil.techassist.data.repository

import com.assanhanil.techassist.data.local.dao.BearingDao
import com.assanhanil.techassist.data.local.entity.BearingEntity
import com.assanhanil.techassist.domain.model.Bearing
import com.assanhanil.techassist.domain.model.BearingSearchResult
import com.assanhanil.techassist.domain.repository.BearingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of BearingRepository using Room database.
 * Provides offline-first bearing lookup functionality.
 */
class BearingRepositoryImpl(
    private val bearingDao: BearingDao
) : BearingRepository {

    override suspend fun findByDimensions(
        innerDiameter: Double,
        outerDiameter: Double,
        width: Double,
        tolerance: Double
    ): BearingSearchResult {
        return try {
            val results = bearingDao.findByDimensions(
                minId = innerDiameter - tolerance,
                maxId = innerDiameter + tolerance,
                minOd = outerDiameter - tolerance,
                maxOd = outerDiameter + tolerance,
                minWidth = width - tolerance,
                maxWidth = width + tolerance
            )

            if (results.isEmpty()) {
                BearingSearchResult.NotFound(
                    "No bearing found matching dimensions: ID=$innerDiameter, OD=$outerDiameter, W=$width (±${tolerance}mm)"
                )
            } else {
                BearingSearchResult.Found(results.map { it.toDomain() })
            }
        } catch (e: Exception) {
            BearingSearchResult.Error(e)
        }
    }

    override suspend fun findByIsoCode(isoCode: String): Bearing? {
        return bearingDao.findByIsoCode(isoCode)?.toDomain()
    }

    override fun getAllBearings(): Flow<List<Bearing>> {
        return bearingDao.getAllBearings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertBearing(bearing: Bearing): Long {
        return bearingDao.insertBearing(bearing.toEntity())
    }

    override suspend fun insertBearings(bearings: List<Bearing>) {
        bearingDao.insertBearings(bearings.map { it.toEntity() })
    }

    // Extension functions for mapping between domain and entity
    private fun BearingEntity.toDomain(): Bearing {
        return Bearing(
            id = id,
            isoCode = isoCode,
            innerDiameter = innerDiameter,
            outerDiameter = outerDiameter,
            width = width,
            type = type,
            sealType = sealType,
            description = description
        )
    }

    private fun Bearing.toEntity(): BearingEntity {
        return BearingEntity(
            id = id,
            isoCode = isoCode,
            innerDiameter = innerDiameter,
            outerDiameter = outerDiameter,
            width = width,
            type = type,
            sealType = sealType,
            description = description
        )
    }
}
