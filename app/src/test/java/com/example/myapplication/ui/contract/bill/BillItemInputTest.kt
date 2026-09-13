package com.example.myapplication.ui.contract.bill

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.toMutableStateList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BillItemInputTest {
    @Test fun saverRetainsEditedExtraRowsAndTheirKeysAfterRemoval() {
        val rows = listOf(
            BillItemInput("Water", "12", false),
            BillItemInput("Remove me", "25", true),
            BillItemInput("Refund", "-", true)
        ).toMutableStateList()
        rows.removeAt(1)
        rows[1].description = "Reimbursement"
        val saved = with(BillItemsSaver) {
            SaverScope { it is String }.save(rows)!!
        }
        val restored = BillItemsSaver.restore(saved)!!
        assertEquals(rows.map { it.key }, restored.map { it.key })
        assertEquals(listOf("Water", "Reimbursement"), restored.map { it.description })
        assertEquals(listOf("12", "-"), restored.map { it.amount })
        assertTrue(restored[1].isExtra)
        restored[1].amount = "-10.25"
        assertEquals("-", rows[1].amount)
    }
}
