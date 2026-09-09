package com.example.myapplication.ui.contract.detail

import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.dao.PaymentDao
import com.example.myapplication.data.entity.*
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.PaymentRepository
import com.example.myapplication.model.Address
import com.example.myapplication.model.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
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
        com.example.myapplication.data.dao.BillDraftDao::class.java.classLoader,
        arrayOf(com.example.myapplication.data.dao.BillDraftDao::class.java)
    ) { _, _, _ -> error("Bill storage is not used by contract detail") }
        as com.example.myapplication.data.dao.BillDraftDao
    private val payments = TestPaymentDao()
    private val viewModels = mutableListOf<ContractDetailViewModel>()

    @Before
    fun setup() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun teardown() {
        viewModels.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }

    private fun viewModel(id: Long) = ContractDetailViewModel(
        id, ContractRepository(contracts, unusedBills), PaymentRepository(payments)
    ).also { viewModels.add(it) }

    @Test
    fun entriesObserveTheirOwnContractsAndPaymentsAndEditsUseTheirFixedId() = runTest {
        contracts.results[1L] = MutableStateFlow(contract(1L))
        contracts.results[2L] = MutableStateFlow(contract(2L))
        val first = viewModel(1L)
        val second = viewModel(2L)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { first.uiState.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { second.uiState.collect {} }
        runCurrent()

        val date = LocalDate.of(2026, 7, 1)
        payments.results.getValue(1L).value = listOf(date, date.plusDays(1))
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
        assertEquals(1, payments.results.getValue(1L).subscriptionCount.value)
        first.cancel()
        second.cancel()
        runCurrent()
        advanceTimeBy(5_000.milliseconds)
        runCurrent()
        assertEquals(0, contracts.results.getValue(1L).subscriptionCount.value)
        assertEquals(0, payments.results.getValue(1L).subscriptionCount.value)
    }

    private fun contract(id: Long): ContractWithRelations {
        val date = LocalDate.of(2026, 7, 1)
        return ContractWithRelations(
            ContractEntity(id, 1L, Tenant("Tenant $id", "123"), BigDecimal("1000"), date.minusMonths(6), date.plusMonths(6)),
            PropertyWithOwner(PropertyEntity(1L, Address("Main", "1", null), 1L), OwnerEntity(1L, "Owner")),
            emptyList(),
            listOf(AdjustmentEntity(id, id, date, BigDecimal("0.10")))
        )
    }

    private class TestContractDao : ContractDao {
        override fun observeWithStatus(monthStart: LocalDate, nextMonthStart: LocalDate): Flow<List<ContractWithStatus>> = error("Unused")
        val results = mutableMapOf<Long, MutableStateFlow<ContractWithRelations?>>()
        override fun observeById(contractId: Long): Flow<ContractWithRelations?> =
            results.getOrPut(contractId) { MutableStateFlow(null) }
        override suspend fun updateAdjustmentAmount(contractId: Long, time: LocalDate, amount: BigDecimal?) {
            val state = results.getValue(contractId)
            state.value = state.value?.let { contract ->
                contract.copy(adjustments = contract.adjustments.map { if (it.time == time) it.copy(amount = amount) else it })
            }
        }
        override fun getAllContracts(): Flow<List<ContractWithRelations>> = error("Detail must query by ID")
        override suspend fun insertContract(contract: ContractEntity): Long = error("Unused")
        override suspend fun insertContractServiceCrossRefs(crossRefs: List<ContractServiceCrossRef>): Unit = error("Unused")
        override suspend fun insertAdjustments(adjustments: List<AdjustmentEntity>): Unit = error("Unused")
    }

    private class TestPaymentDao : PaymentDao {
        val results = mutableMapOf<Long, MutableStateFlow<List<LocalDate>>>()
        override fun observePaymentDates(contractId: Long): Flow<List<LocalDate>> =
            results.getOrPut(contractId) { MutableStateFlow(emptyList()) }
        override fun getPaymentsForContract(contractId: Long): Flow<List<PaymentWithServices>> = error("Unused")
        override suspend fun insertPayment(payment: PaymentEntity): Long = error("Unused")
        override suspend fun insertServicePayments(servicePayments: List<ServicePaymentEntity>): Unit = error("Unused")
    }
}
