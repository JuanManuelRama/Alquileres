package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.dao.BillDao
import com.example.myapplication.model.Contract
import com.example.myapplication.data.entity.ContractWithStatus
import java.time.YearMonth
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContractRepository(private val dao: ContractDao, private val bills: BillDao) {
    fun observeWithStatus(month: YearMonth): Flow<List<ContractWithStatus>> =
        dao.observeWithStatus(month.atDay(1), month.plusMonths(1).atDay(1))

    fun observeById(contractId: Long): Flow<Contract?> =
        dao.observeById(contractId).map { it?.toDomain() }

    fun getAll(): Flow<List<Contract>> {
        return dao.getAllContracts().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insert(contract: Contract): Contract {
        val id = bills.createContract(contract, dao)
        return contract.copy(id = id)
    }

    suspend fun updateAdjustmentAmount(contractId: Long, time: LocalDate, amount: BigDecimal?) {
        dao.updateAdjustmentAmount(contractId, time, amount)
    }
}
