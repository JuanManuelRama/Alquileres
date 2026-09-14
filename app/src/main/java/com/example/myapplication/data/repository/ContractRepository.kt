package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.BillDao
import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.entity.ContractWithStatus
import com.example.myapplication.model.Contract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.YearMonth

class ContractRepository(private val dao: ContractDao, private val bills: BillDao) {
    fun observeWithStatus(month: YearMonth): Flow<List<ContractWithStatus>> =
        dao.observeWithStatus(month)

    fun observeInactive(): Flow<List<Contract>> = dao.observeInactive().map { contracts ->
        contracts.map { it.toDomain() }
    }

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

    suspend fun updateAdjustmentAmount(contractId: Long, time: YearMonth, amount: BigDecimal?) {
        dao.updateAdjustmentAmount(contractId, time, amount)
    }

    suspend fun finish(contractId: Long, requireAllPaid: Boolean) {
        check(dao.finish(contractId, requireAllPaid) == 1) {
            "Could not finish this contract. Check that all bills are paid and try again."
        }
    }
}
