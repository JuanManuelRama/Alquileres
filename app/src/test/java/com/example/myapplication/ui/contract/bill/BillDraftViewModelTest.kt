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

    @Test fun formEditsAndNavigationNeverWriteUntilSubmitted() = runTest {
        val vm = model()
        runCurrent()
        val input = vm.uiState.value.draft!!.copy(rentInput = "1234",
            services = listOf(BillDraftService(7, "Water", "0")))
        advanceTimeBy(5000.milliseconds)
        vm.selectMonth(month.plusMonths(1))
        vm.selectMonth(month)
        runCurrent()
        assertTrue(store.writes.isEmpty())
        assertEquals("1000", vm.uiState.value.draft!!.rentInput)
        vm.save(input)
        runCurrent()
        assertEquals(input.rentInput, store.writes.single().rentInput)
        assertTrue(vm.uiState.value.saved)
        vm.selectMonth(month.plusMonths(1))
        vm.selectMonth(month)
        assertEquals("1234", vm.uiState.value.draft!!.rentInput)
        val reopened = model()
        runCurrent()
        assertEquals("1234", reopened.uiState.value.draft!!.rentInput)
    }

    @Test fun invalidOrDifferentBillCannotBeSubmitted() = runTest {
        val vm = model()
        runCurrent()
        val draft = vm.uiState.value.draft!!
        vm.save(draft)
        vm.save(draft.copy(rentInput = "0", services = emptyList()))
        vm.save(draft.copy(services = listOf(BillDraftService(7, "Water", "-"))))
        vm.save(draft.copy(id = 999, services = emptyList()))
        runCurrent()
        assertTrue(store.writes.isEmpty())
    }

    @Test fun failedSubmissionLeavesSavedBillUntouchedAndCanBeResubmitted() = runTest {
        val vm = model()
        runCurrent()
        val input = vm.uiState.value.draft!!.copy(rentInput = "1500",
            services = listOf(BillDraftService(7, "Water", "25")))
        store.failSave = true
        vm.save(input)
        runCurrent()
        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.saved)
        assertNotNull(vm.uiState.value.error)
        assertEquals("1000", vm.uiState.value.draft!!.rentInput)
        store.failSave = false
        vm.save(input)
        runCurrent()
        assertTrue(vm.uiState.value.saved)
        assertNull(vm.uiState.value.error)
        assertEquals("1500", vm.uiState.value.draft!!.rentInput)
    }

    @Test fun duplicateSubmitsAndNavigationAreBlockedDuringSave() = runTest {
        val vm = model()
        runCurrent()
        val input = vm.uiState.value.draft!!.copy(services = emptyList())
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
        assertNull(vm.uiState.value.draft)
        store.failLoad = false
        vm.retry()
        runCurrent()
        assertNotNull(vm.uiState.value.draft)
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
