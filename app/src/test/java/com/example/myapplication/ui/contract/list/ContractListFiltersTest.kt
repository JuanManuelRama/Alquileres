package com.example.myapplication.ui.contract.list

import com.example.myapplication.model.Address
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class ContractListFiltersTest {
    private fun item(id: Long, ownerId: Long, street: String, due: Boolean, expiry: Boolean, hike: Boolean) =
        ContractListItem(
            Contract(
                property = Property(Address(street, "123", "B"), Owner("Same name", ownerId)),
                tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
                startingDate = LocalDate.of(2026, 1, 1), finishingDate = LocalDate.of(2027, 1, 1),
                adjustments = emptyList(), services = emptyList(), id = id
            ), due, expiry, hike
        )
    private val all = listOf(
        item(1, 10, "Oak", true, true, true),
        item(2, 20, "Oak", true, true, false),
        item(3, 10, "Pine", false, false, true)
    )

    @Test
    fun searchIsTrimmedAndCaseInsensitiveAndOwnersUseIds() {
        assertEquals(listOf(all[0], all[1]), all.filtered("  oAK  ", null, false, false, false))
        assertEquals(listOf(all[1]), all.filtered("Oak", 20, false, false, false))
        assertEquals(all, all.filtered("123 B", null, false, false, false))
    }

    @Test
    fun flagsIntersectAndClearingRestoresEverything() {
        assertEquals(listOf(all[0], all[1]), all.filtered("", null, true, true, false))
        assertEquals(listOf(all[0]), all.filtered("", null, true, true, true))
        assertEquals(emptyList<ContractListItem>(), all.filtered("Pine", null, true, false, false))
        assertEquals(all, all.filtered("", null, false, false, false))
    }
}
