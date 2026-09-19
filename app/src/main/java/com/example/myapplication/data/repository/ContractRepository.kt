package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.BillDao
import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.entity.ContractWithStatus
import com.example.myapplication.data.entity.itemEntities
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Contract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.YearMonth

class ContractRepository(private val dao: ContractDao, private val billDao: BillDao) {
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

    suspend fun insert(contract: Contract, renewingContractId: Long? = null): Contract {
        val id = dao.saveContract(
            contract = contract.toEntity(),
            serviceIds = contract.services.map { it.id },
            adjustments = contract.adjustments.map { it.toEntity() },
            bills = contract.createBills().associate { bill -> bill.toEntity() to bill.itemEntities() },
            billDao = billDao,
            renewingContractId = renewingContractId
        )
        return contract.copy(id = id)
    }

    suspend fun updateAdjustmentAmount(contractId: Long, time: YearMonth, amount: BigDecimal?) {
        check(dao.updateAdjustmentAmount(contractId, time, amount) == 1) {
            "Adjustment no longer exists, its contract is finished, or an affected bill is already paid"
        }
    }

    suspend fun finish(contractId: Long, requireAllPaid: Boolean, cancellationMonth: YearMonth = YearMonth.now()) {
        check(dao.finish(contractId, requireAllPaid, cancellationMonth) == 1) {
            "Could not finish this contract. Check that all bills are paid and try again."
        }
    }

    suspend fun delete(contractId: Long) {
        check(dao.deleteContract(contractId) == 1) {
            "Could not delete this contract."
        }
    }
}
