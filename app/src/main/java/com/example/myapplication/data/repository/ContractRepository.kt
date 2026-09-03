package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Contract
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContractRepository(private val dao: ContractDao) {
    fun getAll(): Flow<List<Contract>> {
        return dao.getAllContracts().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insert(contract: Contract): Contract {
        val id = dao.saveContract(
            contract = contract.toEntity(),
            serviceIds = contract.services.map { it.id },
            adjustments = contract.adjustments.map { it.toEntity() }
        )
        return contract.copy(id = id)
    }

    suspend fun updateAdjustmentAmount(contractId: Long, time: LocalDate, amount: BigDecimal?) {
        dao.updateAdjustmentAmount(contractId, time, amount)
    }
}
