package com.example.myapplication.data.repository

import androidx.room3.withWriteTransaction
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entity.ContractWithStatus
import com.example.myapplication.data.entity.itemEntities
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Contract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.YearMonth

interface ContractWriter {
    suspend fun insert(contract: Contract, renewingContractId: Long? = null): Contract
}

interface ContractDetailsStore {
    fun observeById(contractId: Long): Flow<Contract?>
    suspend fun updateAdjustmentAmount(contractId: Long, time: YearMonth, amount: BigDecimal?)
    suspend fun finish(contractId: Long)
    suspend fun cancel(contractId: Long, cancellationMonth: YearMonth = YearMonth.now())
    suspend fun delete(contractId: Long)
}

class ContractRepository(private val database: AppDatabase) : ContractWriter, ContractDetailsStore {
    private val dao = database.contractDao()
    private val billDao = database.billDao()
    private val ownerDao = database.ownerDao()
    private val propertyDao = database.propertyDao()
    private val serviceDao = database.serviceDao()
    fun observeWithStatus(month: YearMonth): Flow<List<ContractWithStatus>> =
        dao.observeWithStatus(month)

    fun observeInactive(): Flow<List<Contract>> = dao.observeInactive().map { contracts ->
        contracts.map { it.toDomain() }
    }

    override fun observeById(contractId: Long): Flow<Contract?> =
        dao.observeById(contractId).map { it?.toDomain() }

    fun getAll(): Flow<List<Contract>> {
        return dao.getAllContracts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insert(contract: Contract, renewingContractId: Long?): Contract = database.withWriteTransaction {
        val property = if (contract.property.id == 0L) {
            val draftOwner = contract.property.owner
            val owner = if (draftOwner.id == 0L) {
                draftOwner.copy(id = ownerDao.insert(draftOwner.toEntity()))
            } else draftOwner
            val draftProperty = contract.property.copy(owner = owner)
            draftProperty.copy(id = propertyDao.insertProperty(draftProperty.toEntity()))
        } else {
            check(propertyDao.isActive(contract.property.id)) { "Property is inactive or no longer exists" }
            contract.property
        }
        val services = contract.services.map { service ->
            if (service.id == 0L) service.copy(id = serviceDao.insert(service.toEntity())) else service
        }
        if (renewingContractId != null) finish(renewingContractId)
        val saved = contract.copy(property = property, services = services)
        val id = dao.saveContract(saved.toEntity(), services.map { it.id }, saved.adjustments.map { it.toEntity() })
        val result = saved.copy(id = id)
        billDao.createAll(result.createBills().associate { it.toEntity() to it.itemEntities() })
        result
    }

    override suspend fun updateAdjustmentAmount(contractId: Long, time: YearMonth, amount: BigDecimal?) {
        check(dao.updateAdjustmentAmount(contractId, time, amount) == 1) {
            "Adjustment no longer exists, its contract is finished, or it is frozen"
        }
    }

    override suspend fun finish(contractId: Long) {
        check(dao.finish(contractId) == 1) {
            "Could not finish this contract."
        }
    }

    override suspend fun cancel(contractId: Long, cancellationMonth: YearMonth) {
        database.withWriteTransaction {
            finish(contractId)
            dao.shortenTerm(contractId, cancellationMonth)
            billDao.deleteFutureBills(contractId, cancellationMonth)
            dao.deleteFutureAdjustments(contractId, cancellationMonth)
        }
    }

    override suspend fun delete(contractId: Long) {
        database.withWriteTransaction {
            check(dao.deleteContract(contractId) == 1) {
                "Could not delete this contract."
            }
            serviceDao.deleteUnreferenced()
        }
    }
}
