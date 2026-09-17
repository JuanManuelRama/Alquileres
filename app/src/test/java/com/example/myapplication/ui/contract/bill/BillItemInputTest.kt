package com.example.myapplication.ui.contract.bill

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BillItemInputTest {
    @Test fun restoredBaselineAndDraftCompareByValueWithMatchingKeys() {
        val baseline = listOf(BillItemInput("Water", "12", false))
        fun restore(rows: List<BillItemInput>): List<BillItemInput> {
            val saved = Json.encodeToString(rows)
            return Json.decodeFromString(saved)
        }
        val restoredBaseline = restore(baseline)
        assertEquals(restoredBaseline, restore(baseline))
        val edited = restore(baseline.map { it.copy(amount = "55.") })
        assertNotEquals(restoredBaseline, edited)
        assertEquals(restoredBaseline, edited.map { it.copy(amount = "12") })
        assertNotEquals(restoredBaseline, edited + BillItemInput("", "", true))
        assertNotEquals(restoredBaseline, emptyList<BillItemInput>())
    }

    @Test fun serializationRetainsEditedExtraRowsAndTheirKeysAfterRemoval() {
        var rows = listOf(
            BillItemInput("Water", "12", false),
            BillItemInput("Remove me", "25", true),
            BillItemInput("Refund", "-", true)
        )
        rows = rows.filterIndexed { index, _ -> index != 1 }
        val original = rows[1]
        rows = rows.map { if (it.key == original.key) it.copy(description = "Reimbursement") else it }
        assertEquals("Refund", original.description)
        val saved = Json.encodeToString(rows)
        var restored = Json.decodeFromString<List<BillItemInput>>(saved)
        assertEquals(rows.map { it.key }, restored.map { it.key })
        assertEquals(listOf("Water", "Reimbursement"), restored.map { it.description })
        assertEquals(listOf("12", "-"), restored.map { it.amount })
        assertTrue(restored[1].isExtra)
        restored = restored.map { if (it.key == original.key) it.copy(amount = "-10.25") else it }
        assertEquals("-10.25", restored[1].amount)
        assertEquals(original.key, restored[1].key)
        assertEquals("-", rows[1].amount)
    }
}
