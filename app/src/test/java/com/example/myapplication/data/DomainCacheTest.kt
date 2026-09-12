package com.example.myapplication.data

import com.example.myapplication.data.entity.*
import com.example.myapplication.model.Address
import com.example.myapplication.model.Tenant
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth
import java.time.LocalDate

class DomainCacheTest {
    private val owner = OwnerEntity(1, "Owner")
    private val property = PropertyEntity(1, Address("Street", "12", "A"), owner.id)

    @Test fun contractMappingPreservesRentCacheAndCopiesRecalculate() {
        val entity = ContractEntity(1, property.id, Tenant("Tenant", "123"),
            BigDecimal("1000"), YearMonth.of(2026, 1), YearMonth.of(2026, 3))
        val relation = ContractWithRelations(entity, PropertyWithOwner(property, owner),
            emptyList(), emptyList())
        val original = relation.toDomain()
        assertSame(original, relation.toDomain())
        assertSame(original.valueMonths, relation.toDomain().valueMonths)
        val updated = relation.copy(contract = entity.copy(rent = BigDecimal("2000"))).toDomain()
        assertNotSame(original, updated)
        assertEquals(BigDecimal("2000"), updated.valueAtIndex(0))
        val basic = BasicContract(entity, property, owner)
        assertEquals(basic.toDomain(), basic.toDomain())
    }

    @Test fun billsReuseContractAndPreservePaidRent() {
        val month = YearMonth.of(2026, 1)
        val entity = ContractEntity(1, property.id, Tenant("Tenant", "123"),
            BigDecimal("2000"), month, month.plusMonths(2))
        val contract = ContractWithRelations(entity, PropertyWithOwner(property, owner),
            emptyList(), emptyList())
        val unpaid = BillWithItems(BillEntity(1, 1, month, updatedAt = 0),
            listOf(BillItemEntity(1, "Water", 0, "12.50")), contract)
        assertNotSame(unpaid.toDomain(), unpaid.toDomain())
        assertSame(contract.toDomain(), unpaid.toDomain().contract)
        assertEquals(BigDecimal("2000"), unpaid.toDomain().rent)
        val paid = unpaid.copy(bill = unpaid.bill.copy(
            rent = BigDecimal("1000"), paidAt = LocalDate.of(2026, 1, 15)))
        assertEquals(BigDecimal("1000"), paid.toDomain().rent)
        assertNotSame(unpaid.toDomain(), paid.toDomain())
        assertEquals(1L, paid.toDomain().toEntity().contractId)
        assertEquals(BigDecimal("1000"), paid.toDomain().toEntity().rent)
        val originalContract = unpaid.toDomain().contract
        val reloaded = paid.copy(contract = contract.copy())
        assertNotSame(originalContract, reloaded.contract.toDomain())
        val saved = reloaded.toDomain(originalContract)
        assertSame(originalContract, saved.contract)
        assertSame(originalContract.valueMonths, saved.contract.valueMonths)
        assertEquals(paid.bill.paidAt, saved.paidAt)
        assertEquals(BigDecimal("1000"), saved.rent)
        assertEquals(BigDecimal("12.50"), saved.items.single().amount)
        val reloadedUnpaid = unpaid.copy(contract = contract.copy()).toDomain(originalContract)
        assertSame(originalContract, reloadedUnpaid.contract)
    }
}
