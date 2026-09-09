package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.BillDraftDao
import com.example.myapplication.data.entity.serviceEntities
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.BillDraft
import java.time.YearMonth

interface BillDraftStore {
    suspend fun list(contractId: Long): List<BillDraft>
    suspend fun open(contractId: Long, month: YearMonth): BillDraft =
        list(contractId).first { it.month == month }
    suspend fun save(draft: BillDraft): BillDraft
}

class BillDraftRepository(
    private val dao: BillDraftDao
) : BillDraftStore {
    override suspend fun list(contractId: Long): List<BillDraft> =
        dao.list(contractId).map { it.toDomain() }

    override suspend fun save(draft: BillDraft): BillDraft {
        val saved = draft.copy(updatedAt = System.currentTimeMillis())
        dao.save(saved.toEntity(), saved.serviceEntities())
        return saved
    }
}
