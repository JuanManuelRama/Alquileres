package com.example.myapplication.ui.contract.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BillDraftStore
import com.example.myapplication.model.BillDraft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

data class BillDraftUiState(
    val month: YearMonth,
    val bills: List<BillDraft> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
) {
    val draft: BillDraft? get() = bills.firstOrNull { it.month == month }
}

class BillDraftViewModel(
    private val contractId: Long,
    private val store: BillDraftStore,
    initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {
    private val _uiState = MutableStateFlow(BillDraftUiState(initialMonth))
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun retry() {
        if (!_uiState.value.isLoading && _uiState.value.draft == null) load()
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val bills = store.list(contractId)
                val month = _uiState.value.month
                val selected = bills.firstOrNull { it.month >= month } ?: bills.lastOrNull()
                checkNotNull(selected) { "No bills for this contract" }
                _uiState.value = BillDraftUiState(selected.month, bills, isLoading = false)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Could not load bills. Please retry.")
            }
        }
    }

    fun selectMonth(month: YearMonth) {
        val state = _uiState.value
        if (state.isLoading || state.isSaving || state.month == month || state.bills.none { it.month == month }) return
        _uiState.value = state.copy(month = month, saved = false, error = null)
    }

    // Only an explicit, valid form submission writes to storage.
    fun save(draft: BillDraft) {
        val state = _uiState.value
        val original = state.draft ?: return
        if (state.isLoading || state.isSaving || !draft.isValid ||
            draft.id != original.id || draft.contractId != contractId || draft.month != state.month) return
        _uiState.value = state.copy(isSaving = true, saved = false, error = null)
        viewModelScope.launch {
            try {
                val saved = store.save(draft)
                _uiState.value = _uiState.value.copy(
                    bills = state.bills.map { if (it.id == saved.id) saved else it },
                    isSaving = false, saved = true
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false, error = "Could not save this bill. Please try Save bill again."
                )
            }
        }
    }
}
