package com.example.myapplication.ui.contract.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BillStore
import com.example.myapplication.model.Bill
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class BillUiState(
    val month: YearMonth,
    val bills: List<Bill> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
) {
    val bill: Bill? get() = bills.firstOrNull { it.month == month }
}

class BillViewModel(
    private val contractId: Long,
    private val store: BillStore,
    initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {
    private val _uiState = MutableStateFlow(BillUiState(initialMonth))
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun retry() {
        if (!_uiState.value.isLoading && _uiState.value.bill == null) load()
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val bills = store.list(contractId)
                val month = _uiState.value.month
                val selected = bills.firstOrNull { it.month >= month } ?: bills.lastOrNull()
                checkNotNull(selected) { "No bills for this contract" }
                _uiState.value = BillUiState(selected.month, bills, isLoading = false)
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
    fun save(bill: Bill) = persist(bill, null)

    fun markPaid(bill: Bill, date: LocalDate) {
        if (date.isAfter(LocalDate.now())) return
        persist(bill, date)
    }

    private fun persist(bill: Bill, paymentDate: LocalDate?) {
        val state = _uiState.value
        val original = state.bill ?: return
        if (state.isLoading || state.isSaving || original.isPaid || bill.isPaid || !bill.isValid ||
            bill.id != original.id || bill.contract.id != contractId || bill.month != state.month) return
        _uiState.value = state.copy(isSaving = true, saved = false, error = null)
        viewModelScope.launch {
            try {
                val saved = if (paymentDate == null) store.save(bill) else store.markPaid(bill, paymentDate)
                _uiState.value = _uiState.value.copy(
                    bills = state.bills.map { if (it.id == saved.id) saved else it },
                    isSaving = false, saved = true
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false, error = if (paymentDate == null) "Could not save this bill. Please try Save bill again."
                    else "Could not confirm payment. " + (error.message ?: "Please retry.")
                )
            }
        }
    }
}
