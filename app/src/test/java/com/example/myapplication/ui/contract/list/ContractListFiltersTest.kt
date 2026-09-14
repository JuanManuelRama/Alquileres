package com.example.myapplication.ui.contract.list

import com.example.myapplication.model.Address
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import androidx.compose.runtime.saveable.SaverScope
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class ContractListFiltersTest {
    private fun item(id: Long, ownerId: Long, street: String, due: Boolean, expiry: Boolean, hike: Boolean) =
        ContractListItem(
            Contract(
                property = Property(Address(street, "123", "B"), Owner("Same name", ownerId)),
                tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
                startingMonth = YearMonth.of(2026, 1), finishingMonth = YearMonth.of(2027, 1),
                adjustments = emptyList(), services = emptyList(), id = id
            ), due, expiry, hike
        )
    private val all = listOf(
        item(1, 10, "Oak", true, true, true),
        item(2, 20, "Oak", true, true, false),
        item(3, 10, "Pine", false, false, true)
    )

    private fun filtered(state: ContractListFilterState) = all.filter { state.matches(it.contract, it) }

    @Test
    fun searchIsTrimmedAndCaseInsensitiveAndOwnersUseIds() {
        assertEquals(listOf(all[0], all[1]), filtered(ContractListFilterState(query = "  oAK  ")))
        assertEquals(listOf(all[1]), filtered(ContractListFilterState(query = "Oak", ownerId = 20)))
        assertEquals(all, filtered(ContractListFilterState(query = "123 B")))
    }

    @Test
    fun flagsIntersectAndClearingRestoresEverything() {
        assertEquals(listOf(all[0], all[1]), filtered(ContractListFilterState(paymentDue = true, expiresSoon = true)))
        assertEquals(listOf(all[0]), filtered(ContractListFilterState(paymentDue = true, expiresSoon = true, hikeThisMonth = true)))
        assertEquals(emptyList<ContractListItem>(), filtered(ContractListFilterState(query = "Pine", paymentDue = true)))
        assertEquals(all, filtered(ContractListFilterState()))
    }

    @Test fun inactiveDateFiltersIncludeOverlappingMonthsWithoutStatusObjects() {
        val state = ContractListFilterState(startingMonth = YearMonth.of(2027, 1))
        assertTrue(state.matches(all[0].contract))
        state.startingMonth = YearMonth.of(2027, 2)
        assertFalse(state.matches(all[0].contract))
        state.startingMonth = null
        state.finishingMonth = YearMonth.of(2025, 12)
        assertFalse(state.matches(all[0].contract))
        state.clear()
        assertFalse(state.hasActiveFilters)
        assertTrue(state.matches(all[0].contract))
    }

    @Test fun savedFiltersRestoreDatesAndClearAllFields() {
        val state = ContractListFilterState("Oak", 10, YearMonth.of(2026, 3), YearMonth.of(2026, 8), true, true, true)
        val saved = with(ContractListFilterState.Saver) { SaverScope { it is String }.save(state)!! }
        val restored = ContractListFilterState.Saver.restore(saved)!!
        assertEquals(filtered(state), filtered(restored))
        assertEquals(state.startingMonth, restored.startingMonth)
        assertEquals(state.finishingMonth, restored.finishingMonth)
        restored.clear()
        assertFalse(restored.hasActiveFilters)
        assertEquals(all, filtered(restored))
        assertTrue(state.hasActiveFilters)
    }
}
