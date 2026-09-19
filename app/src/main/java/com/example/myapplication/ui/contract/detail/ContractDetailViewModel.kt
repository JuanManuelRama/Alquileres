package com.example.myapplication.ui.contract.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BillStore
import com.example.myapplication.data.repository.ContractDetailsStore
import com.example.myapplication.model.Contract
import com.example.myapplication.ui.SaveStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    ) : ContractDetailUiState {
        val allBillsPaid: Boolean get() = contract.startingMonth <= contract.finishingMonth &&
            generateSequence(contract.startingMonth) { it.plusMonths(1) }
                .takeWhile { it <= contract.finishingMonth }.all { it in paidMonths }
    }
}

class ContractDetailViewModel(
    private val contractId: Long,
    private val contractRepository: ContractDetailsStore,
    billStore: BillStore
) : ViewModel() {
    private val _writeState = MutableStateFlow(ContractWriteState())
    val writeState = _writeState.asStateFlow()
    val uiState: StateFlow<ContractDetailUiState> = combine(
        contractRepository.observeById(contractId),
        billStore.observePaidMonths(contractId),
    ) { contract, paidMonths ->
        if (contract == null) ContractDetailUiState.NotFound
        else ContractDetailUiState.Ready(contract, paidMonths)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContractDetailUiState.Loading)

    fun updateAdjustmentAmount(time: YearMonth, amount: BigDecimal?) = write(ContractWriteAction.Adjustment, time) {
        contractRepository.updateAdjustmentAmount(contractId, time, amount)
    }

    fun finishContract() {
        val state = uiState.value as? ContractDetailUiState.Ready ?: return
        if (!state.allBillsPaid) return
        write(ContractWriteAction.Finish) { contractRepository.finish(contractId) }
    }

    fun cancelContract(cancellationMonth: YearMonth = YearMonth.now()) = write(ContractWriteAction.Cancel) {
        contractRepository.cancel(contractId, cancellationMonth)
    }

    fun deleteContract() = write(ContractWriteAction.Delete) { contractRepository.delete(contractId) }

    fun clearWriteResult() {
        if (_writeState.value.status != SaveStatus.Saving) _writeState.value = ContractWriteState()
    }

    private fun write(action: ContractWriteAction, month: YearMonth? = null, block: suspend () -> Unit) {
        if (uiState.value !is ContractDetailUiState.Ready || _writeState.value.isBusy) return
        _writeState.value = ContractWriteState(action, month, SaveStatus.Saving)
        viewModelScope.launch {
            try {
                block()
                _writeState.value = ContractWriteState(action, month, SaveStatus.Saved)
            } catch (error: CancellationException) {
                _writeState.value = ContractWriteState()
                throw error
            } catch (_: Exception) {
                _writeState.value = ContractWriteState(action, month, SaveStatus.Failed)
            }
        }
    }
}

enum class ContractWriteAction { Adjustment, Finish, Cancel, Delete }

data class ContractWriteState(
    val action: ContractWriteAction? = null,
    val month: YearMonth? = null,
    val status: SaveStatus = SaveStatus.Idle,
) {
    val isBusy: Boolean get() = status == SaveStatus.Saving || status == SaveStatus.Saved
}