package com.example.myapplication.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import com.example.myapplication.data.entity.BillDraftEntity
import com.example.myapplication.data.entity.BillDraftServiceEntity
import com.example.myapplication.data.entity.BillDraftWithServices

import com.example.myapplication.model.BillDraft
import com.example.myapplication.model.Contract
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.data.entity.serviceEntities

@Dao
interface BillDraftDao {
    @Transaction
    @Query("SELECT * FROM BillDraft WHERE contractId = :contractId ORDER BY billingMonth")
    suspend fun list(contractId: Long): List<BillDraftWithServices>

    @Transaction
    suspend fun createAll(drafts: List<BillDraft>) {
        drafts.forEach { draft ->
            val id = insertDraft(draft.toEntity())
            insertServices(draft.serviceEntities().map { it.copy(draftId = id) })
        }
    }

    @Transaction
    suspend fun createContract(contract: Contract, contracts: ContractDao): Long {
        val id = contracts.saveContract(contract.toEntity(), contract.services.map { it.id },
            contract.adjustments.map { it.toEntity() })
        createAll(contract.copy(id = id).createBills())
        return id
    }

    @Transaction
    @Query("SELECT * FROM BillDraft WHERE contractId = :contractId AND billingMonth = :month")
    suspend fun find(contractId: Long, month: String): BillDraftWithServices?

    @Insert
    suspend fun insertDraft(draft: BillDraftEntity): Long

    @Insert
    suspend fun insertServices(services: List<BillDraftServiceEntity>)

    @Update
    suspend fun updateDraft(draft: BillDraftEntity): Int

    @Query("DELETE FROM BillDraftService WHERE draftId = :id")
    suspend fun deleteServices(id: Long)

    @Transaction
    suspend fun save(draft: BillDraftEntity, services: List<BillDraftServiceEntity>) {
        check(updateDraft(draft) == 1) { "Bill draft no longer exists" }
        deleteServices(draft.id)
        insertServices(services)
    }
}
