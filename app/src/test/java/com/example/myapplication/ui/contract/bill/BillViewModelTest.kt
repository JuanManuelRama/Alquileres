package com.example.myapplication.ui.contract.bill

import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BillStore
import com.example.myapplication.model.Bill
import com.example.myapplication.model.BillItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.YearMonth
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class BillViewModelTest {
    private val month = YearMonth.of(2026, 12)
    private val store = FakeStore()
    private val models = mutableListOf<BillViewModel>()

    @Before fun setup() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun teardown() {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }
    private fun model(id: Long = 1) = BillViewModel(id, store, month).also { models.add(it) }

    @Test fun formEditsAndNavigationNeverWriteUntilSubmitted() = runTest {
        val vm = model()
        runCurrent()
        val input = vm.uiState.value.bill!!.copy(rentInput = "1234",
            items = listOf(BillItem("Water", "0")))
        advanceTimeBy(5000.milliseconds)
        vm.selectMonth(month.plusMonths(1))
        vm.selectMonth(month)
        runCurrent()
        assertTrue(store.writes.isEmpty())
        assertEquals("1000", vm.uiState.value.bill!!.rentInput)
        vm.save(input)
        runCurrent()
        assertEquals(input.rentInput, store.writes.single().rentInput)
        assertTrue(vm.uiState.value.saved)
        vm.selectMonth(month.plusMonths(1))
        vm.selectMonth(month)
        assertEquals("1234", vm.uiState.value.bill!!.rentInput)
        val reopened = model()
        runCurrent()
        assertEquals("1234", reopened.uiState.value.bill!!.rentInput)
    }

    @Test fun invalidOrDifferentBillCannotBeSubmitted() = runTest {
        val vm = model()
        runCurrent()
        val bill = vm.uiState.value.bill!!
        vm.save(bill)
        vm.save(bill.copy(rentInput = "0", items = emptyList()))
        vm.save(bill.copy(items = listOf(BillItem("Water", "-"))))
        vm.save(bill.copy(id = 999, items = emptyList()))
        runCurrent()
        assertTrue(store.writes.isEmpty())
    }

    @Test fun failedSubmissionLeavesSavedBillUntouchedAndCanBeResubmitted() = runTest {
        val vm = model()
        runCurrent()
        val input = vm.uiState.value.bill!!.copy(rentInput = "1500",
            items = listOf(BillItem("Water", "25")))
        store.failSave = true
        vm.save(input)
        runCurrent()
        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.saved)
        assertNotNull(vm.uiState.value.error)
        assertEquals("1000", vm.uiState.value.bill!!.rentInput)
        store.failSave = false
        vm.save(input)
        runCurrent()
        assertTrue(vm.uiState.value.saved)
        assertNull(vm.uiState.value.error)
        assertEquals("1500", vm.uiState.value.bill!!.rentInput)
    }

    @Test fun duplicateSubmitsAndNavigationAreBlockedDuringSave() = runTest {
        val vm = model()
        runCurrent()
        val input = vm.uiState.value.bill!!.copy(items = emptyList())
        val gate = CompletableDeferred<Unit>()
        store.gate = gate
        vm.save(input)
        vm.save(input)
        vm.selectMonth(month.plusMonths(1))
        runCurrent()
        assertTrue(vm.uiState.value.isSaving)
        assertEquals(month, vm.uiState.value.month)
        gate.complete(Unit)
        runCurrent()
        assertEquals(1, store.writes.size)
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test fun failedLoadCanBeRetried() = runTest {
        store.failLoad = true
        val vm = model()
        runCurrent()
        assertNull(vm.uiState.value.bill)
        store.failLoad = false
        vm.retry()
        runCurrent()
        assertNotNull(vm.uiState.value.bill)
        assertNull(vm.uiState.value.error)
    }

    @Test fun navigationStaysWithinPreloadedBills() = runTest {
        val vm = model()
        runCurrent()
        vm.selectMonth(month.minusMonths(1))
        assertEquals(month, vm.uiState.value.month)
        vm.selectMonth(month.plusMonths(2))
        vm.selectMonth(month.plusMonths(3))
        assertEquals(month.plusMonths(2), vm.uiState.value.month)
        assertEquals(3, store.records.size)
        assertTrue(store.writes.isEmpty())
    }

    @Test fun confirmationFreezesBillAndPreventsDuplicatePayments() = runTest {
        val vm = model()
        runCurrent()
        val input = vm.uiState.value.bill!!.copy(items = listOf(BillItem("Water", "25")))
        val date = LocalDate.now()
        vm.markPaid(input, date)
        vm.markPaid(input, date)
        runCurrent()
        assertEquals(date, vm.uiState.value.bill!!.paidAt)
        vm.save(input.copy(rentInput = "999"))
        vm.markPaid(input, date)
        runCurrent()
        assertEquals(1, store.writes.size)
        val reopened = model()
        runCurrent()
        assertTrue(reopened.uiState.value.bill!!.isPaid)
    }

    @Test fun invalidFutureOrFailedPaymentDoesNotLockBill() = runTest {
        val vm = model()
        runCurrent()
        val original = vm.uiState.value.bill!!
        vm.markPaid(original, LocalDate.now())
        val input = original.copy(items = listOf(BillItem("Water", "0")))
        vm.markPaid(input, LocalDate.now().plusDays(1))
        runCurrent()
        assertTrue(store.writes.isEmpty())
        store.failSave = true
        vm.markPaid(input, LocalDate.now())
        runCurrent()
        assertFalse(vm.uiState.value.bill!!.isPaid)
        assertNotNull(vm.uiState.value.error)
        store.failSave = false
        vm.markPaid(input, LocalDate.now())
        runCurrent()
        assertTrue(vm.uiState.value.bill!!.isPaid)
    }

    private class FakeStore : BillStore {
        override fun observePaidMonths(contractId: Long): kotlinx.coroutines.flow.Flow<Set<YearMonth>> = error("Unused")
        val records = mutableMapOf<Pair<Long, YearMonth>, Bill>()
        val writes = mutableListOf<Bill>()
        var gate: CompletableDeferred<Unit>? = null
        var failSave = false
        var failLoad = false
        var withServices = true
        override suspend fun list(contractId: Long): List<Bill> =
            (0L..2L).map { open(contractId, YearMonth.of(2026, 12).plusMonths(it)) }

        override suspend fun open(contractId: Long, month: YearMonth): Bill {
            check(!failLoad)
            return records.getOrPut(contractId to month) {
                Bill(records.size.toLong() + 1, contractId, month, "1000",
                    if (withServices) listOf(BillItem("Water")) else emptyList(), 1)
            }
        }
        override suspend fun markPaid(bill: Bill, date: LocalDate): Bill =
            save(bill.copy(paidAt = date))

        override suspend fun save(bill: Bill): Bill {
            gate?.await()
            check(!failSave)
            writes.add(bill)
            return bill.copy(updatedAt = bill.updatedAt + 1).also { records[bill.contractId to bill.month] = it }
        }
    }
}
