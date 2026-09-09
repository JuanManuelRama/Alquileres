package com.example.myapplication.ui.contract.bill

import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BillDraftStore
import com.example.myapplication.model.BillDraft
import com.example.myapplication.model.BillDraftService
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
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class BillDraftViewModelTest {
    private val month = YearMonth.of(2026, 12)
    private val store = FakeStore()
    private val models = mutableListOf<BillDraftViewModel>()

    @Before fun setup() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun teardown() {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }
    private fun model(id: Long = 1) = BillDraftViewModel(id, store, month).also { models.add(it) }

    @Test fun autosaveWaitsForPauseAndPreservesPartialInputs() = runTest {
        val vm = model()
        assertTrue(vm.uiState.value.isLoading)
        runCurrent()
        assertFalse(vm.uiState.value.readyToSend)
        vm.changeService(7, "12.")
        advanceTimeBy(499.milliseconds)
        runCurrent()
        assertTrue(store.writes.isEmpty())
        vm.changeService(7, "-")
        advanceTimeBy(500.milliseconds)
        runCurrent()
        assertEquals("-", store.writes.single().services.single().amountInput)
        assertEquals(DraftSaveStatus.Saved, vm.uiState.value.saveStatus)
        assertFalse(vm.uiState.value.readyToSend)
        val reopened = model()
        runCurrent()
        assertEquals("-", reopened.uiState.value.draft!!.services.single().amountInput)
    }

    @Test fun readyRequiresValidAmountsAndSuccessfulSaveIncludingZeroServices() = runTest {
        val vm = model()
        runCurrent()
        vm.changeService(7, "0")
        assertFalse(vm.uiState.value.readyToSend)
        vm.savePending()
        runCurrent()
        assertTrue(vm.uiState.value.readyToSend)
        vm.changeRent("0")
        vm.savePending()
        runCurrent()
        assertFalse(vm.uiState.value.readyToSend)
        vm.changeRent("1200")
        vm.changeService(7, "")
        vm.savePending()
        runCurrent()
        assertFalse(vm.uiState.value.readyToSend)
    }

    @Test fun emptyServiceListNeedsOnlyValidSavedRent() = runTest {
        store.withServices = false
        val vm = model()
        runCurrent()
        assertTrue(vm.uiState.value.readyToSend)
    }

    @Test fun switchingMonthFlushesAndReopeningRestoresEachMonthsValues() = runTest {
        val vm = model()
        runCurrent()
        vm.changeRent("1234")
        vm.selectMonth(month.plusMonths(1))
        runCurrent()
        assertEquals("1234", store.records.getValue(1L to month).rentInput)
        assertEquals(month.plusMonths(1), vm.uiState.value.month)
        vm.changeRent("2345")
        vm.selectMonth(month)
        runCurrent()
        assertEquals("1234", vm.uiState.value.draft!!.rentInput)
        assertEquals("2345", store.records.getValue(1L to month.plusMonths(1)).rentInput)
        val other = model(2)
        runCurrent()
        assertEquals("1000", other.uiState.value.draft!!.rentInput)
    }

    @Test fun failedSaveRetainsEditsBlocksLeavingAndCanBeRetried() = runTest {
        val vm = model()
        runCurrent()
        store.failSave = true
        vm.changeRent("1500")
        var left = false
        vm.leave { left = true }
        runCurrent()
        assertFalse(left)
        assertEquals(DraftSaveStatus.Error, vm.uiState.value.saveStatus)
        assertEquals("1500", vm.uiState.value.draft!!.rentInput)
        vm.selectMonth(month.plusMonths(1))
        runCurrent()
        assertEquals(month, vm.uiState.value.month)
        store.failSave = false
        vm.retry()
        runCurrent()
        assertEquals(DraftSaveStatus.Saved, vm.uiState.value.saveStatus)
        vm.leave { left = true }
        runCurrent()
        assertTrue(left)
    }

    @Test fun editsDuringSlowSaveAreWrittenAfterItAndBackWaitsForLatest() = runTest {
        val vm = model()
        runCurrent()
        val gate = CompletableDeferred<Unit>()
        store.gate = gate
        vm.changeRent("1100")
        advanceTimeBy(500.milliseconds)
        runCurrent()
        assertEquals(DraftSaveStatus.Saving, vm.uiState.value.saveStatus)
        vm.changeRent("2200")
        var left = false
        vm.leave { left = true }
        runCurrent()
        assertFalse(left)
        gate.complete(Unit)
        runCurrent()
        assertTrue(left)
        assertEquals(listOf("1100", "2200"), store.writes.map { it.rentInput })
        assertEquals("2200", store.records.getValue(1L to month).rentInput)
    }

    @Test fun backgroundFlushDoesNotWaitForDebounce() = runTest {
        val vm = model()
        runCurrent()
        vm.changeRent("1800")
        vm.savePending()
        runCurrent()
        assertEquals("1800", store.writes.single().rentInput)
    }

    @Test fun failedLoadCanBeRetried() = runTest {
        store.failLoad = true
        val vm = model()
        runCurrent()
        assertNull(vm.uiState.value.draft)
        assertNotNull(vm.uiState.value.error)
        store.failLoad = false
        vm.retry()
        runCurrent()
        assertNotNull(vm.uiState.value.draft)
        assertNull(vm.uiState.value.error)
    }

    @Test fun navigationStaysWithinPreloadedBills() = runTest {
        val vm = model()
        runCurrent()
        val ids = vm.uiState.value.bills.map { it.id }
        vm.selectMonth(month.minusMonths(1))
        runCurrent()
        assertEquals(month, vm.uiState.value.month)
        vm.selectMonth(month.plusMonths(2))
        runCurrent()
        vm.selectMonth(month.plusMonths(3))
        runCurrent()
        assertEquals(month.plusMonths(2), vm.uiState.value.month)
        assertEquals(ids, vm.uiState.value.bills.map { it.id })
        assertEquals(3, store.records.size)
    }

    private class FakeStore : BillDraftStore {
        val records = mutableMapOf<Pair<Long, YearMonth>, BillDraft>()
        val writes = mutableListOf<BillDraft>()
        var gate: CompletableDeferred<Unit>? = null
        var failSave = false
        var failLoad = false
        var withServices = true
        override suspend fun list(contractId: Long): List<BillDraft> =
            (0L..2L).map { open(contractId, YearMonth.of(2026, 12).plusMonths(it)) }

        override suspend fun open(contractId: Long, month: YearMonth): BillDraft {
            check(!failLoad)
            return records.getOrPut(contractId to month) {
                BillDraft(records.size.toLong() + 1, contractId, month, "1000",
                    if (withServices) listOf(BillDraftService(7, "Water")) else emptyList(), 1)
            }
        }
        override suspend fun save(draft: BillDraft): BillDraft {
            gate?.await()
            check(!failSave)
            writes.add(draft)
            return draft.copy(updatedAt = draft.updatedAt + 1).also { records[draft.contractId to draft.month] = it }
        }
    }
}
