package com.example.myapplication.data.entity

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import com.example.myapplication.model.BillDraft
import com.example.myapplication.model.BillDraftService
import java.time.YearMonth

@Entity(
    tableName = "BillDraft",
    foreignKeys = [ForeignKey(entity = ContractEntity::class, parentColumns = ["id"],
        childColumns = ["contractId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["contractId", "billingMonth"], unique = true)]
)
data class BillDraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contractId: Long,
    val billingMonth: String,
    val rentInput: String,
    val updatedAt: Long
)

@Entity(
    tableName = "BillDraftService",
    primaryKeys = ["draftId", "serviceId"],
    foreignKeys = [
        ForeignKey(
            entity = BillDraftEntity::class,
            parentColumns = ["id"],
            childColumns = ["draftId"],
            onDelete = ForeignKey.CASCADE)]
)
data class BillDraftServiceEntity(
    val draftId: Long,
    val serviceId: Long,
    val serviceName: String,
    val position: Int,
    val amountInput: String
)

data class BillDraftWithServices(
    @Embedded val draft: BillDraftEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["draftId"])
    val services: List<BillDraftServiceEntity>
) {
    fun toDomain(): BillDraft = BillDraft(
        draft.id, draft.contractId, YearMonth.parse(draft.billingMonth), draft.rentInput,
        services.sortedBy { it.position }.map { BillDraftService(it.serviceId, it.serviceName, it.amountInput) },
        draft.updatedAt
    )
}

fun BillDraft.toEntity() = BillDraftEntity(id, contractId, month.toString(), rentInput, updatedAt)
fun BillDraft.serviceEntities() = services.mapIndexed { index, service ->
    BillDraftServiceEntity(id, service.serviceId, service.name, index, service.amountInput)
}
