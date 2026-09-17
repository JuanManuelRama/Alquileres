package com.example.myapplication.ui.contract.detail

import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.entity.AdjustmentEntity
import com.example.myapplication.data.entity.BasicContract
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.data.entity.ContractServiceCrossRef
import com.example.myapplication.data.entity.ContractWithRelations
import com.example.myapplication.data.entity.ContractWithStatus
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.PropertyWithOwner
import com.example.myapplication.data.repository.BillStore
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.model.Address
import com.example.myapplication.model.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ContractDetailViewModelTest {
    private val contracts = TestContractDao()
    private val unusedBills = java.lang.reflect.Proxy.newProxyInstance(
        com.example.myapplication.data.dao.BillDao::class.java.classLoader,
        arrayOf(com.example.myapplication.data.dao.BillDao::class.java)
    ) { _, _, _ -> error("Bill storage is not used by contract detail") }
        as com.example.myapplication.data.dao.BillDao
    private val bills = TestBillStore()
    private val viewModels = mutableListOf<ContractDetailViewModel>()

    @Before
    fun setup() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun teardown() {
        viewModels.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    private fun viewModel(id: Long) = ContractDetailViewModel(
        id, ContractRepository(contracts, unusedBills), bills
    ).also { viewModels.add(it) }

    @Test
    fun entriesObserveTheirOwnContractsAndBillsAndEditsUseTheirFixedId() = runTest {
        contracts.results[1L] = MutableStateFlow(contract(1L))
        contracts.results[2L] = MutableStateFlow(contract(2L))
        val first = viewModel(1L)
        val second = viewModel(2L)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { first.uiState.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { second.uiState.collect {} }
        runCurrent()

        val date = YearMonth.of(2026, 7)
        bills.results.getValue(1L).value = listOf(date, date)
        first.updateAdjustmentAmount(date, BigDecimal("0.20"))
        runCurrent()

        val firstState = first.uiState.value as ContractDetailUiState.Ready
        val secondState = second.uiState.value as ContractDetailUiState.Ready
        assertEquals(1L, firstState.contract.id)
        assertEquals(setOf(YearMonth.of(2026, 7)), firstState.paidMonths)
        assertEquals(BigDecimal("0.20"), firstState.contract.adjustments.single().amount)
        assertEquals(2L, secondState.contract.id)
        assertTrue(secondState.paidMonths.isEmpty())
        assertEquals(BigDecimal("0.10"), secondState.contract.adjustments.single().amount)
    }

    @Test
    fun missingOrRemovedContractDoesNotKeepShowingStaleData() = runTest {
        val model = viewModel(3L)
        assertEquals(ContractDetailUiState.Loading, model.uiState.value)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.uiState.collect {} }
        runCurrent()
        assertEquals(ContractDetailUiState.NotFound, model.uiState.value)

        contracts.results.getValue(3L).value = contract(3L)
        runCurrent()
        assertTrue(model.uiState.value is ContractDetailUiState.Ready)
        contracts.results.getValue(3L).value = null
        runCurrent()
        assertEquals(ContractDetailUiState.NotFound, model.uiState.value)
    }

    @Test
    fun multipleCollectorsShareSourcesAndObservationStopsAfterLastCollectorLeaves() = runTest {
        val model = viewModel(1L)
        val first = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.uiState.collect {} }
        val second = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.uiState.collect {} }
        runCurrent()
        assertEquals(1, contracts.results.getValue(1L).subscriptionCount.value)
        assertEquals(1, bills.results.getValue(1L).subscriptionCount.value)
        first.cancel()
        second.cancel()
        runCurrent()
        advanceTimeBy(5_000.milliseconds)
        runCurrent()
        assertEquals(0, contracts.results.getValue(1L).subscriptionCount.value)
        assertEquals(0, bills.results.getValue(1L).subscriptionCount.value)
    }

    private fun contract(id: Long): ContractWithRelations {
        val date = YearMonth.of(2026, 7)
        return ContractWithRelations(
            ContractEntity(id, 1L, Tenant("Tenant $id", "123"), BigDecimal("1000"), date.minusMonths(6), date.plusMonths(6)),
            PropertyWithOwner(PropertyEntity(1L, Address("Main", "1", null), 1L), OwnerEntity(1L, "Owner")),
            emptyList(),
            listOf(AdjustmentEntity(id, date, BigDecimal("0.10")))
        )
    }

    @Test fun finishNeedsEveryMonthPaidWhileCancellationDoesNot() = runTest {
        var finished = false
        contracts.results[1L] = MutableStateFlow(contract(1L))
        val model = viewModel(1L)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.uiState.collect {} }
        runCurrent()
        model.finishContract(requireAllPaid = true, onFinished = { finished = true })
        runCurrent()
        assertFalse(finished)
        val activeContract = (model.uiState.value as ContractDetailUiState.Ready).contract
        bills.results.getValue(1L).value = generateSequence(activeContract.startingMonth) { it.plusMonths(1) }
            .takeWhile { it <= activeContract.finishingMonth }.toList()
        runCurrent()
        assertTrue((model.uiState.value as ContractDetailUiState.Ready).allBillsPaid)
        model.finishContract(requireAllPaid = true, onFinished = { finished = true })
        runCurrent()
        assertTrue(finished)

        contracts.results[2L] = MutableStateFlow(contract(2L))
        val other = viewModel(2L)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { other.uiState.collect {} }
        runCurrent()
        var cancelled = false
        other.finishContract(requireAllPaid = false, onFinished = { cancelled = true })
        runCurrent()
        assertTrue(cancelled)
        assertTrue((other.uiState.value as ContractDetailUiState.Ready).paidMonths.isEmpty())
    }

    @Test fun deleteContractDeletesDataAndInvokesCallback() = runTest {
        var deleted = false
        contracts.results[1L] = MutableStateFlow(contract(1L))
        val model = viewModel(1L)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.uiState.collect {} }
        runCurrent()
        assertTrue(model.uiState.value is ContractDetailUiState.Ready)
        model.deleteContract { deleted = true }
        runCurrent()
        assertTrue(deleted)
        assertEquals(ContractDetailUiState.NotFound, model.uiState.value)
    }

    private class TestContractDao : ContractDao {
        override suspend fun deleteContract(contractId: Long): Int {
            val state = results[contractId] ?: return 0
            state.value = null
            return 1
        }
        override suspend fun updateContractFinished(contractId: Long, requireAllPaid: Boolean, cancellationMonth: YearMonth): Int = error("Unused")
        override suspend fun deleteFutureBills(contractId: Long, cancellationMonth: YearMonth): Int = 0
        override suspend fun deleteFutureAdjustments(contractId: Long, cancellationMonth: YearMonth): Int = 0

        override suspend fun finish(contractId: Long, requireAllPaid: Boolean, cancellationMonth: YearMonth): Int {
            val state = results.getValue(contractId)
            val current = state.value ?: return 0
            val newFinishingMonth = if (!requireAllPaid && cancellationMonth < current.contract.finishingMonth) cancellationMonth else current.contract.finishingMonth
            state.value = current.copy(contract = current.contract.copy(isFinished = true, finishingMonth = newFinishingMonth))
            return 1
        }
        override fun observeInactive(): Flow<List<BasicContract>> = error("Unused")
        override fun observeWithStatus(month: YearMonth): Flow<List<ContractWithStatus>> = error("Unused")
        val results = mutableMapOf<Long, MutableStateFlow<ContractWithRelations?>>()
        override fun observeById(contractId: Long): Flow<ContractWithRelations?> =
            results.getOrPut(contractId) { MutableStateFlow(null) }
        override suspend fun updateAdjustmentAmount(contractId: Long, time: YearMonth, amount: BigDecimal?): Int {
            val state = results.getValue(contractId)
            state.value = state.value?.let { contract ->
                contract.copy(adjustments = contract.adjustments.map { if (it.time == time) it.copy(amount = amount) else it })
            }
            return state.value?.adjustments?.count { it.time == time } ?: 0
        }
        override fun getAllContracts(): Flow<List<ContractWithRelations>> = error("Detail must query by ID")
        override suspend fun insertContract(contract: ContractEntity): Long = error("Unused")
        override suspend fun insertContractService(contractRefService: List<ContractServiceCrossRef>): Unit = error("Unused")
        override suspend fun insertAdjustments(adjustments: List<AdjustmentEntity>): Unit = error("Unused")
    }

    private class TestBillStore : BillStore {
        val results = mutableMapOf<Long, MutableStateFlow<List<YearMonth>>>()
        override fun observePaidMonths(contractId: Long): Flow<Set<YearMonth>> =
            results.getOrPut(contractId) { MutableStateFlow(emptyList()) }
                .map { dates -> dates.toSet() }
        override suspend fun list(contractId: Long): List<com.example.myapplication.model.Bill> = error("Unused")
        override suspend fun save(bill: com.example.myapplication.model.Bill): com.example.myapplication.model.Bill = error("Unused")
        override suspend fun markPaid(bill: com.example.myapplication.model.Bill, date: LocalDate): com.example.myapplication.model.Bill = error("Unused")
    }
}
