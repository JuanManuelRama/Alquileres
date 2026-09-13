package com.example.myapplication.ui.contract

import androidx.compose.runtime.snapshots.Snapshot
import com.example.myapplication.ui.contract.create.ContractInputs
import org.junit.Assert.assertEquals
import org.junit.Test

class InputStateTest {
    @Test
    fun contractEditsAreObservableWithoutReplacingTheHolder() {
        val inputs = ContractInputs(tenantName = "Before", rent = "100")
        val snapshot = Snapshot.takeSnapshot()
        try {
            inputs.tenantName = "After"
            inputs.rent = "55."

            assertEquals("After", inputs.tenantName)
            assertEquals("55.", inputs.rent)
            snapshot.enter {
                assertEquals("Before", inputs.tenantName)
                assertEquals("100", inputs.rent)
            }
        } finally {
            snapshot.dispose()
        }
    }

}
