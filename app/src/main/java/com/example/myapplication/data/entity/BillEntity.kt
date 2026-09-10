package com.example.myapplication.data.entity

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import com.example.myapplication.model.Bill
import com.example.myapplication.model.BillItem
import java.time.LocalDate
import java.time.YearMonth

@Entity(
    tableName = "Bill",
    foreignKeys = [
        ForeignKey(
            entity = ContractEntity::class,
            parentColumns = ["id"],
            childColumns = ["contractId"], onDelete = ForeignKey.CASCADE
        )],
    indices = [
        Index(
            value = ["contractId", "billingMonth"],
            unique = true
        )]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contractId: Long,
    val billingMonth: YearMonth,
    val rentInput: String,
    val updatedAt: Long,
    val paidAt: LocalDate? = null
)

@Entity(
    tableName = "BillItem",
    primaryKeys = ["billId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE)]
)
data class BillItemEntity(
    val billId: Long,
    val description: String,
    val position: Int,
    val amount: String
)

data class BillWithItems(
    @Embedded val bill: BillEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["billId"])
    val items: List<BillItemEntity>
) {
    fun toDomain(): Bill = Bill(
        id = bill.id,
        contractId = bill.contractId,
        month = bill.billingMonth,
        rentInput = bill.rentInput,
        items = items.sortedBy { it.position }.map { BillItem(it.description, it.amount) },
        updatedAt = bill.updatedAt,
        paidAt = bill.paidAt

    )
}

fun Bill.toEntity() = BillEntity(
    id = id,
    contractId = contractId,
    billingMonth = month,
    rentInput = rentInput,
    updatedAt = updatedAt,
    paidAt = paidAt
)
fun Bill.itemEntities() = items.mapIndexed { index, item ->
    BillItemEntity(id, item.description, index, item.amount)
}
