package com.example.myapplication.ui.contract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.PaymentRepository
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Payment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.LocalDate
import java.time.ZoneId
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import com.example.myapplication.ui.contract.list.ContractListItem
import com.example.myapplication.ui.contract.list.listItem
import kotlin.time.Duration.Companion.milliseconds

class ContractViewModel(
    private val contractRepository: ContractRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    // Re-evaluate the calendar while observed, including when the screen crosses midnight.
    private val currentMonth = flow {
        while (true) {
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            emit(YearMonth.from(today))
            val midnight = today.plusDays(1).atStartOfDay(zone).toInstant()
            delay(Duration.between(Instant.now(), midnight).toMillis().coerceAtLeast(1).milliseconds)
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val listItems: StateFlow<List<ContractListItem>> = currentMonth.flatMapLatest { month ->
        contractRepository.observeWithStatus(month).map { contracts ->
            contracts.map { it.details.toDomain().listItem(month, it.paymentDue, it.hikeThisMonth) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val contracts: StateFlow<List<Contract>> = contractRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun paymentsFor(contract: Contract): Flow<List<Payment>> =
        paymentRepository.getPaymentsForContract(contract)

    fun paidMonthsFor(contractId: Long): Flow<Set<YearMonth>> =
        paymentRepository.observePaidMonths(contractId)

    fun savePayment(payment: Payment) {
        viewModelScope.launch {
            paymentRepository.save(payment)
        }
    }
}
