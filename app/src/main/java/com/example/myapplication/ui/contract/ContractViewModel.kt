package com.example.myapplication.ui.contract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.model.Contract
import com.example.myapplication.ui.contract.list.ContractListItem
import com.example.myapplication.ui.contract.list.ContractListUiState
import com.example.myapplication.ui.contract.list.listItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

class ContractViewModel(
    private val contractRepository: ContractRepository
) : ViewModel() {
    // Re-evaluate the calendar while observed, including when the screen crosses into a new month.
    private val currentMonth = flow {
        while (true) {
            val zone = ZoneId.systemDefault()
            val month = YearMonth.now(zone)
            emit(month)
            val midnight = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()
            delay(Duration.between(Instant.now(), midnight).toMillis().coerceAtLeast(1).milliseconds)
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val listState: StateFlow<ContractListUiState> = currentMonth.flatMapLatest { month ->
        contractRepository.observeWithStatus(month).map { contracts ->
            contracts.map { it.details.toDomain().listItem(month, it.paymentDue, it.hikeThisMonth) }
        }
    }.map<List<ContractListItem>, ContractListUiState> { ContractListUiState.Loaded(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContractListUiState.Loading)

    val contracts: StateFlow<List<Contract>> = contractRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val inactiveContracts: StateFlow<List<Contract>?> = contractRepository.observeInactive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

}
