package ar.controlalq.ui.contract.bill

import androidx.lifecycle.viewModelScope
import ar.controlalq.data.repository.BillStore
import ar.controlalq.model.Address
import ar.controlalq.model.Bill
import ar.controlalq.model.BillItem
import ar.controlalq.model.Contract
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.model.Tenant
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
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

    @Test fun unchangedBillsDoNotWriteAndSavedEditsSurviveNavigation() = runTest {
        val vm = model()
        runCurrent()
        val input = vm.uiState.value.bill!!.copy(
            rent = 1234.toBigDecimal(),
            items = listOf(BillItem("Water", BigDecimal.ZERO))
        )
        advanceTimeBy(5000.milliseconds)
        vm.selectMonth(month.plusMonths(1))
        vm.selectMonth(month)
        runCurrent()
        assertTrue(store.writes.isEmpty())
        assertEquals(BigDecimal("1000"), vm.uiState.value.bill!!.rent)
        vm.save(input)
        runCurrent()
        assertEquals(input.rent, store.writes.single().rent)
        assertTrue(vm.uiState.value.saved)
        vm.selectMonth(month.plusMonths(1))
        vm.selectMonth(month)
        assertEquals(BigDecimal("1234"), vm.uiState.value.bill!!.rent)
        val reopened = model()
        runCurrent()
        assertEquals(BigDecimal("1234"), reopened.uiState.value.bill!!.rent)
    }

    @Test fun invalidOrDifferentBillCannotBeSubmitted() = runTest {
        val vm = model()
        runCurrent()
        val bill = vm.uiState.value.bill!!
        vm.save(bill)
        vm.save(bill.copy(items = listOf(BillItem("Water", BigDecimal("1.234")))))
        vm.save(bill.copy(items = listOf(BillItem("Bad\nname", null))))
        vm.save(bill.copy(id = 999, items = emptyList()))
        runCurrent()
        assertTrue(store.writes.isEmpty())
    }

    @Test fun emptyAndPartialRowsAreSavedButCannotBePaid() = runTest {
        val vm = model()
        runCurrent()
        val draft = vm.uiState.value.bill!!.copy(items = listOf(
            BillItem("Water", null), BillItem("", null, true), BillItem("", BigDecimal("25"), true)
        ))
        vm.save(draft)
        runCurrent()
        assertEquals(draft.items, store.writes.single().items)
        vm.markPaid(draft, LocalDate.now())
        runCurrent()
        assertFalse(vm.uiState.value.bill!!.isPaid)
        assertEquals(1, store.writes.size)
    }

    @Test fun editsDuringSaveKeepLatestIncludingRevertingToOriginal() = runTest {
        val vm = model()
        runCurrent()
        val original = vm.uiState.value.bill!!
        val gate = CompletableDeferred<Unit>()
        store.gate = gate
        vm.save(original.copy(items = listOf(BillItem("Water", BigDecimal("25")))))
        runCurrent()
        vm.save(original.copy(items = listOf(BillItem("Water", BigDecimal("50")))))
        vm.save(original)
        gate.complete(Unit)
        runCurrent()
        assertEquals(2, store.writes.size)
        assertEquals(original.items, store.writes.last().items)
        assertTrue(vm.uiState.value.saved)
    }

    @Test fun failedAutosaveCanBeRetriedWithoutReenteringInput() = runTest {
        val vm = model()
        runCurrent()
        val draft = vm.uiState.value.bill!!.copy(items = listOf(BillItem("Extra", null, true)))
        store.failSave = true
        vm.save(draft)
        runCurrent()
        vm.selectMonth(month.plusMonths(1))
        assertEquals(month, vm.uiState.value.month)
        store.failSave = false
        vm.retry()
        runCurrent()
        assertEquals(draft.items, store.writes.single().items)
        assertTrue(vm.uiState.value.saved)
    }

    @Test fun failedSubmissionLeavesSavedBillUntouchedAndCanBeResubmitted() = runTest {
        val vm = model()
        runCurrent()
        val input = vm.uiState.value.bill!!.copy(rent = BigDecimal("1500"),
            items = listOf(BillItem("Water", BigDecimal("25"))))
        store.failSave = true
        vm.save(input)
        runCurrent()
        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.saved)
        assertNotNull(vm.uiState.value.error)
        assertEquals(BigDecimal("1000"), vm.uiState.value.bill!!.rent)
        store.failSave = false
        vm.save(input)
        runCurrent()
        assertTrue(vm.uiState.value.saved)
        assertNull(vm.uiState.value.error)
        assertEquals(BigDecimal("1500"), vm.uiState.value.bill!!.rent)
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
        val input = vm.uiState.value.bill!!.copy(items = listOf(BillItem("Water", BigDecimal("25"))))
        val date = LocalDate.now()
        vm.markPaid(input, date)
        vm.markPaid(input, date)
        runCurrent()
        assertEquals(date, vm.uiState.value.bill!!.paidAt)
        vm.save(input.copy(rent = BigDecimal("999")))
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
        val input = original.copy(items = listOf(BillItem("Water", BigDecimal.ZERO)))
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
                val contract = Contract(
                    property = Property(Address("Street", "12", null), Owner("Owner", 1), 1),
                    tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
                    startingMonth = YearMonth.of(2026, 12), finishingMonth = YearMonth.of(2027, 2),
                    adjustments = emptyList(), services = emptyList(), id = contractId
                )
                Bill(records.size.toLong() + 1, contract, month, BigDecimal("1000"),
                    if (withServices) listOf(BillItem("Water", null)) else emptyList(), 1)
            }
        }
        override suspend fun markPaid(bill: Bill, date: LocalDate): Bill =
            save(bill.copy(paidAt = date))

        override suspend fun save(bill: Bill): Bill {
            gate?.await()
            check(!failSave)
            writes.add(bill)
            return bill.copy(updatedAt = bill.updatedAt + 1).also { records[bill.contract.id to bill.month] = it }
        }
    }
}
