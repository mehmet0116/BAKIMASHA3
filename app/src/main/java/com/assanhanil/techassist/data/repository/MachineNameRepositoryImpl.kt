package com.assanhanil.techassist.data.repository

import com.assanhanil.techassist.data.local.dao.MachineNameDao
import com.assanhanil.techassist.data.local.entity.MachineNameEntity
import com.assanhanil.techassist.domain.model.MachineName
import com.assanhanil.techassist.domain.repository.MachineNameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of MachineNameRepository using Room database.
 */
class MachineNameRepositoryImpl(
    private val machineNameDao: MachineNameDao
) : MachineNameRepository {

    override fun getAllMachineNames(): Flow<List<MachineName>> {
        return machineNameDao.getAllMachineNames().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMachineNameById(id: Long): MachineName? {
        return machineNameDao.getMachineNameById(id)?.toDomain()
    }

    override suspend fun getMachineNameByName(name: String): MachineName? {
        return machineNameDao.getMachineNameByName(name)?.toDomain()
    }

    override suspend fun saveMachineName(name: String): Long {
        val entity = MachineNameEntity(name = name)
        return machineNameDao.insertMachineName(entity)
    }

    override suspend fun deleteMachineName(id: Long) {
        machineNameDao.deleteMachineNameById(id)
    }

    private fun MachineNameEntity.toDomain(): MachineName {
        return MachineName(
            id = id,
            name = name,
            createdAt = createdAt
        )
    }
}
