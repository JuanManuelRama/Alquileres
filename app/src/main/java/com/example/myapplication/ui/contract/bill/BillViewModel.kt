package com.example.myapplication.ui.contract.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BillStore
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.message.MessageSender
import com.example.myapplication.model.Bill
import com.example.myapplication.ui.SaveStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class BillUiState(
    val month: YearMonth,
    val bills: List<Bill> = emptyList(),
    val isLoading: Boolean = true,
    val saveStatus: SaveStatus = SaveStatus.Idle,
    val isPaying: Boolean = false,
    val error: BillError? = null
) {
    val isSaving: Boolean get() = saveStatus == SaveStatus.Saving
    val saved: Boolean get() = saveStatus == SaveStatus.Saved
    val bill: Bill? get() = bills.firstOrNull { it.month == month }
}

class BillViewModel(
    private val contractId: Long,
    private val store: BillStore,
    initialMonth: YearMonth = YearMonth.now(),
    settingsRepository: SettingsRepository? = null,
) : ViewModel() {
    val messageSender: StateFlow<MessageSender> = settingsRepository?.messageSender
        ?: MutableStateFlow(MessageSender.WhatsApp).asStateFlow()

    private val _uiState = MutableStateFlow(BillUiState(initialMonth))
    val uiState = _uiState.asStateFlow()
    private var pendingSave: Bill? = null
    private var writingBill: Bill? = null

    init { load() }

    fun retry() {
        if (!_uiState.value.isLoading && _uiState.value.bill == null) load()
        else if (_uiState.value.saveStatus == SaveStatus.Failed) pendingSave?.let(::save)
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
                _uiState.value = _uiState.value.copy(isLoading = false, error = BillError.Load)
            }
        }
    }

    fun selectMonth(month: YearMonth) {
        val state = _uiState.value
        if (state.isLoading || state.isSaving || pendingSave != null || state.month == month || state.bills.none { it.month == month }) return
        _uiState.value = state.copy(month = month, saveStatus = SaveStatus.Idle, error = null)
    }

    // Keep only the latest queued edit, without cancelling an in-flight database write.
    fun save(bill: Bill) {
        val state = _uiState.value
        val original = state.bill ?: return
        if (state.isLoading || state.isPaying || original.isPaid || bill.isPaid ||
            bill.id != original.id || bill.contract.id != contractId || bill.month != state.month ||
            bill.items.any { !BillItemInput(it.description, it.amount?.toPlainString().orEmpty(), it.isExtra).isValidForSaving }) return
        if (!state.isSaving && bill == original && pendingSave == null) return
        if (state.isSaving && bill == (pendingSave ?: writingBill)) return
        pendingSave = bill
        if (state.isSaving) return
        _uiState.value = state.copy(saveStatus = SaveStatus.Saving, error = null)
        viewModelScope.launch {
            while (pendingSave != null) {
                val draft = checkNotNull(pendingSave)
                pendingSave = null
                writingBill = draft
                try {
                    val saved = store.save(draft)
                    _uiState.value = _uiState.value.copy(
                        bills = _uiState.value.bills.map { if (it.id == saved.id) saved else it }
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    if (pendingSave == null) pendingSave = draft
                    _uiState.value = _uiState.value.copy(saveStatus = SaveStatus.Failed, error = BillError.Save)
                    return@launch
                } finally {
                    writingBill = null
                }
            }
            _uiState.value = _uiState.value.copy(saveStatus = SaveStatus.Saved, error = null)
        }
    }

    fun markPaid(bill: Bill, date: LocalDate) {
        if (date.isAfter(LocalDate.now())) return
        persist(bill, date)
    }

    private fun persist(bill: Bill, paymentDate: LocalDate) {
        val state = _uiState.value
        val original = state.bill ?: return
        if (state.isLoading || state.isSaving || pendingSave != null || original.isPaid || bill.isPaid || !bill.isValid ||
            bill.id != original.id || bill.contract.id != contractId || bill.month != state.month) return
        _uiState.value = state.copy(saveStatus = SaveStatus.Saving, isPaying = true, error = null)
        viewModelScope.launch {
            try {
                val saved = store.markPaid(bill, paymentDate)
                _uiState.value = _uiState.value.copy(
                    bills = state.bills.map { if (it.id == saved.id) saved else it },
                    saveStatus = SaveStatus.Saved,
                    isPaying = false
                )
            } catch (error: CancellationException) {
                _uiState.value = _uiState.value.copy(saveStatus = SaveStatus.Idle, isPaying = false)
                throw error
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveStatus = SaveStatus.Failed,
                    isPaying = false,
                    error = BillError.Payment
                )
            }
        }
    }
}

enum class BillError { Load, Save, Payment }
