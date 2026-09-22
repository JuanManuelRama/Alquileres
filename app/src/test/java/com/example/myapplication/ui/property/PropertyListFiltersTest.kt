package com.example.myapplication.ui.property

import com.example.myapplication.model.Address
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class PropertyListFiltersTest {
    private val occupied = PropertyListItem(Property(Address("Oak", "42", "2B"), Owner("Ada", 1), 1), true, true)
    private val vacant = PropertyListItem(Property(Address("Pine", "7", null), Owner("Ada", 1), 2), true, false)
    private val inactive = PropertyListItem(Property(Address("Oak", "9", null), Owner("Ben", 2), 3), false, false)
    private val rows = listOf(occupied, vacant, inactive)

    @Test fun searchTrimsWhitespaceAndMatchesAddressCaseInsensitively() {
        assertEquals(listOf(occupied, inactive), rows.filter(PropertyListFilterState(query = " oAK ")::matches))
        assertEquals(listOf(occupied), rows.filter(PropertyListFilterState(query = "42 2b")::matches))
        assertFalse(PropertyListFilterState(query = "Ada").matches(occupied))
    }

    @Test fun booleanFiltersIncludeBothPositiveAndNegativeMatches() {
        assertTrue(rows.all { PropertyListFilterState().matches(it) } )
        assertEquals(listOf(occupied, vacant), rows.filter(PropertyListFilterState(isActive = true)::matches))
        assertEquals(listOf(inactive), rows.filter(PropertyListFilterState(isActive = false)::matches))
        assertEquals(listOf(occupied), rows.filter(PropertyListFilterState(hasActiveContracts = true)::matches))
        assertEquals(listOf(vacant, inactive), rows.filter(PropertyListFilterState(hasActiveContracts = false)::matches))
    }

    @Test fun allFiltersCombineAndImpossibleCombinationsProduceNoMatches() {
        val filters = PropertyListFilterState(query = "pine", ownerId = 1, isActive = true, hasActiveContracts = false)
        assertEquals(listOf(vacant), rows.filter(filters::matches))
        val modifiedFilters = filters.copy(ownerId = 2)
        assertTrue(rows.none { modifiedFilters.matches(it) })
        assertTrue(
            rows.none(
                PropertyListFilterState(
                    isActive = false,
                    hasActiveContracts = true
                )::matches
            )
        )
        assertTrue(filters.hasActiveFilters)
        assertFalse(PropertyListFilterState(query = "  ").hasActiveFilters)
    }

    @Test fun savedFiltersPreserveFalseSelectionsAndOwner() {
        val filters = PropertyListFilterState("Oak", 2, isActive = false, hasActiveContracts = false)
        val restored = Json.decodeFromString<PropertyListFilterState>(Json.encodeToString(PropertyListFilterState.serializer(), filters))
        assertEquals(filters, restored)
        assertEquals(listOf(inactive), rows.filter(restored::matches))
        assertTrue(restored.hasActiveFilters)
    }
}
