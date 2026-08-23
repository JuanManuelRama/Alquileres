package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Contract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContractRepository(private val dao: ContractDao) {
    fun getAll(): Flow<List<Contract>> {
        return dao.getAllContracts().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insert(contract: Contract, propertyId: Long, tenantId: Long, serviceIds: List<Long>) {
        dao.saveContract(
            contract = contract.toEntity(propertyId, tenantId),
            serviceIds = serviceIds,
            adjustments = contract.adjustments.map { it.toEntity(0) }
        )
    }
}