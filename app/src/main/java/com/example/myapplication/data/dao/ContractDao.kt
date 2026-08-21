package com.example.myapplication.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.data.entity.ContractServiceCrossRef
import com.example.myapplication.data.entity.ContractWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractDao {
    @Transaction
    @Query("SELECT * FROM Contract")
    fun getAllContracts(): Flow<List<ContractWithRelations>>

    @Insert
    suspend fun insertContract(contract: ContractEntity): Long

    @Insert
    suspend fun insertContractServiceCrossRefs(crossRefs: List<ContractServiceCrossRef>)

    @Transaction
    suspend fun saveContract(contract: ContractEntity, serviceIds: List<Long>) {
        val contractId = insertContract(contract)
        val crossRefs = serviceIds.map { ContractServiceCrossRef(contractId, it) }
        insertContractServiceCrossRefs(crossRefs)
    }
}