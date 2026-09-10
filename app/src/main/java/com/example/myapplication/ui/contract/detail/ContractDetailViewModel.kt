package com.example.myapplication.ui.contract.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.BillStore
import com.example.myapplication.model.Contract
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

sealed interface ContractDetailUiState {
    data object Loading : ContractDetailUiState
    data object NotFound : ContractDetailUiState
    data class Ready(
        val contract: Contract,
        val paidMonths: Set<YearMonth>
    ) : ContractDetailUiState
}

class ContractDetailViewModel(
    private val contractId: Long,
    private val contractRepository: ContractRepository,
    billStore: BillStore
) : ViewModel() {
    val uiState: StateFlow<ContractDetailUiState> = combine(
        contractRepository.observeById(contractId),
        billStore.observePaidMonths(contractId)
    ) { contract, paidMonths ->
        if (contract == null) ContractDetailUiState.NotFound
        else ContractDetailUiState.Ready(contract, paidMonths)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ContractDetailUiState.Loading
    )

    fun updateAdjustmentAmount(time: LocalDate, amount: BigDecimal?) {
        viewModelScope.launch {
            contractRepository.updateAdjustmentAmount(contractId, time, amount)
        }
    }
}
