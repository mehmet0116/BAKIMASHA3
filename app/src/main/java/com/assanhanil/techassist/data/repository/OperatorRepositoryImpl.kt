package com.assanhanil.techassist.data.repository

import com.assanhanil.techassist.data.local.dao.OperatorDao
import com.assanhanil.techassist.data.local.entity.OperatorEntity
import com.assanhanil.techassist.domain.model.Operator
import com.assanhanil.techassist.domain.repository.OperatorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of OperatorRepository using Room database.
 * Provides operator storage functionality.
 */
class OperatorRepositoryImpl(
    private val operatorDao: OperatorDao
) : OperatorRepository {

    override fun getAllOperators(): Flow<List<Operator>> {
        return operatorDao.getAllOperators().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchOperators(query: String): Flow<List<Operator>> {
        return operatorDao.searchOperators(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getOperatorById(id: Long): Operator? {
        return operatorDao.getOperatorById(id)?.toDomain()
    }

    override suspend fun getOperatorByName(name: String): Operator? {
        return operatorDao.getOperatorByName(name)?.toDomain()
    }

    override suspend fun saveOperator(operator: Operator): Long {
        return operatorDao.insertOperator(operator.toEntity())
    }

    override suspend fun deleteOperator(operator: Operator) {
        operatorDao.deleteOperator(operator.toEntity())
    }

    override suspend fun deactivateOperator(operatorId: Long) {
        operatorDao.deactivateOperator(operatorId)
    }

    override suspend fun getOperatorsByIds(ids: List<Long>): List<Operator> {
        return operatorDao.getOperatorsByIds(ids).map { it.toDomain() }
    }

    // Extension functions for mapping between domain and entity
    private fun OperatorEntity.toDomain(): Operator {
        return Operator(
            id = id,
            name = name,
            department = department,
            createdAt = createdAt,
            isActive = isActive
        )
    }

    private fun Operator.toEntity(): OperatorEntity {
        return OperatorEntity(
            id = id,
            name = name,
            department = department,
            createdAt = createdAt,
            isActive = isActive
        )
    }
}
