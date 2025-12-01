package com.assanhanil.techassist.domain.repository

import com.assanhanil.techassist.domain.model.Operator
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for operator operations.
 */
interface OperatorRepository {
    
    /**
     * Get all active operators as a Flow.
     */
    fun getAllOperators(): Flow<List<Operator>>
    
    /**
     * Search operators by name.
     */
    fun searchOperators(query: String): Flow<List<Operator>>
    
    /**
     * Get a single operator by ID.
     */
    suspend fun getOperatorById(id: Long): Operator?
    
    /**
     * Get a single operator by name.
     */
    suspend fun getOperatorByName(name: String): Operator?
    
    /**
     * Save (insert or update) an operator.
     */
    suspend fun saveOperator(operator: Operator): Long
    
    /**
     * Delete an operator permanently.
     */
    suspend fun deleteOperator(operator: Operator)
    
    /**
     * Deactivate an operator (soft delete).
     */
    suspend fun deactivateOperator(operatorId: Long)
    
    /**
     * Get operators by their IDs.
     */
    suspend fun getOperatorsByIds(ids: List<Long>): List<Operator>
}
