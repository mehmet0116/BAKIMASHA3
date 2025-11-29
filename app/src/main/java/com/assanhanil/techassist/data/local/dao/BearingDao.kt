package com.assanhanil.techassist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.assanhanil.techassist.data.local.entity.BearingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for bearing operations.
 */
@Dao
interface BearingDao {

    /**
     * Find bearings by dimensions with tolerance.
     */
    @Query("""
        SELECT * FROM bearings 
        WHERE innerDiameter BETWEEN :minId AND :maxId
        AND outerDiameter BETWEEN :minOd AND :maxOd
        AND width BETWEEN :minWidth AND :maxWidth
    """)
    suspend fun findByDimensions(
        minId: Double,
        maxId: Double,
        minOd: Double,
        maxOd: Double,
        minWidth: Double,
        maxWidth: Double
    ): List<BearingEntity>

    /**
     * Find a bearing by ISO code.
     */
    @Query("SELECT * FROM bearings WHERE isoCode = :isoCode LIMIT 1")
    suspend fun findByIsoCode(isoCode: String): BearingEntity?

    /**
     * Get all bearings.
     */
    @Query("SELECT * FROM bearings ORDER BY isoCode ASC")
    fun getAllBearings(): Flow<List<BearingEntity>>

    /**
     * Insert a bearing.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBearing(bearing: BearingEntity): Long

    /**
     * Insert multiple bearings.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBearings(bearings: List<BearingEntity>)

    /**
     * Get count of bearings in database.
     */
    @Query("SELECT COUNT(*) FROM bearings")
    suspend fun getCount(): Int
}
