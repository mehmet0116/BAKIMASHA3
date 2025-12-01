package com.assanhanil.techassist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.assanhanil.techassist.data.local.entity.OperatorEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for operator operations.
 * Allows users to save and manage operators.
 */
@Dao
interface OperatorDao {

    /**
     * Get all active operators.
     */
    @Query("SELECT * FROM operators WHERE isActive = 1 ORDER BY name ASC")
    fun getAllOperators(): Flow<List<OperatorEntity>>

    /**
     * Search operators by name.
     */
    @Query("SELECT * FROM operators WHERE isActive = 1 AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchOperators(query: String): Flow<List<OperatorEntity>>

    /**
     * Get an operator by ID.
     */
    @Query("SELECT * FROM operators WHERE id = :id")
    suspend fun getOperatorById(id: Long): OperatorEntity?

    /**
     * Get an operator by name.
     */
    @Query("SELECT * FROM operators WHERE name = :name AND isActive = 1 LIMIT 1")
    suspend fun getOperatorByName(name: String): OperatorEntity?

    /**
     * Insert a new operator (returns the generated ID).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperator(operator: OperatorEntity): Long

    /**
     * Update an existing operator.
     */
    @Update
    suspend fun updateOperator(operator: OperatorEntity)

    /**
     * Delete an operator.
     */
    @Delete
    suspend fun deleteOperator(operator: OperatorEntity)

    /**
     * Soft delete (deactivate) an operator.
     */
    @Query("UPDATE operators SET isActive = 0 WHERE id = :operatorId")
    suspend fun deactivateOperator(operatorId: Long)

    /**
     * Get count of operators.
     */
    @Query("SELECT COUNT(*) FROM operators WHERE isActive = 1")
    suspend fun getCount(): Int

    /**
     * Get operators by IDs.
     */
    @Query("SELECT * FROM operators WHERE id IN (:ids) AND isActive = 1")
    suspend fun getOperatorsByIds(ids: List<Long>): List<OperatorEntity>
}
