package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.model.Contract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContractRepository(private val dao: ContractDao) {
    fun getAll(): Flow<List<Contract>> {
        return dao.getAllContracts().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun save(contract: Contract) {
        val entity = ContractEntity(
            propertyId = contract.property.id!!,
            tenantId = contract.tenant.id!!,
            initialRent = contract.initialRent,
            currentRent = contract.currentRent,
            startingDate = contract.startingDate,
            finishingDate = contract.finishingDate,
            adjustmentTime = contract.adjustmentTime
        )
        val serviceIds = contract.services.map { it.id!! }
        dao.saveContract(entity, serviceIds)
    }
}