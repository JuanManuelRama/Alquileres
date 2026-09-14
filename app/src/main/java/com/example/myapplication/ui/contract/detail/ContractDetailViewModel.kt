package com.example.myapplication.ui.contract.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BillStore
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.model.Contract
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth

sealed interface ContractDetailUiState {
    data object Loading : ContractDetailUiState
    data object NotFound : ContractDetailUiState
    data class Ready(
        val contract: Contract,
        val paidMonths: Set<YearMonth>,
        val isFinishing: Boolean = false,
        val error: String? = null
    ) : ContractDetailUiState {
        val allBillsPaid: Boolean get() = contract.startingMonth <= contract.finishingMonth &&
            generateSequence(contract.startingMonth) { it.plusMonths(1) }
                .takeWhile { it <= contract.finishingMonth }.all { it in paidMonths }
    }
}

class ContractDetailViewModel(
    private val contractId: Long,
    private val contractRepository: ContractRepository,
    billStore: BillStore
) : ViewModel() {
    private data class FinishState(val saving: Boolean = false, val error: String? = null)
    private val finishState = MutableStateFlow(FinishState())
    val uiState: StateFlow<ContractDetailUiState> = combine(
        contractRepository.observeById(contractId),
        billStore.observePaidMonths(contractId),
        finishState
    ) { contract, paidMonths, action ->
        if (contract == null) ContractDetailUiState.NotFound
        else ContractDetailUiState.Ready(contract, paidMonths, action.saving, action.error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ContractDetailUiState.Loading
    )

    fun updateAdjustmentAmount(time: YearMonth, amount: BigDecimal?) {
        viewModelScope.launch {
            contractRepository.updateAdjustmentAmount(contractId, time, amount)
        }
    }

    fun finishContract(requireAllPaid: Boolean, onFinished: () -> Unit, cancellationMonth: YearMonth = YearMonth.now()) {
        val state = uiState.value as? ContractDetailUiState.Ready ?: return
        if (finishState.value.saving || (requireAllPaid && !state.allBillsPaid)) return
        finishState.value = FinishState(saving = true)
        viewModelScope.launch {
            try {
                contractRepository.finish(contractId, requireAllPaid, cancellationMonth)
                onFinished()
                finishState.value = FinishState()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                finishState.value = FinishState(error = "Could not finish the contract. Please try again.")
            }
        }
    }

    fun deleteContract(onDeleted: () -> Unit) {
        if (uiState.value !is ContractDetailUiState.Ready) return
        if (finishState.value.saving) return
        finishState.value = FinishState(saving = true)
        viewModelScope.launch {
            try {
                contractRepository.delete(contractId)
                onDeleted()
                finishState.value = FinishState()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                finishState.value = FinishState(error = "Could not delete the contract. Please try again.")
            }
        }
    }
}
