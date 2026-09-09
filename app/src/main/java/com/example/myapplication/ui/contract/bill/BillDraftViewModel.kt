package com.example.myapplication.ui.contract.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BillDraftStore
import com.example.myapplication.model.BillDraft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.YearMonth
import kotlin.time.Duration.Companion.milliseconds

enum class DraftSaveStatus { Saved, Unsaved, Saving, Error }

data class BillDraftUiState(
    val month: YearMonth,
    val draft: BillDraft? = null,
    val bills: List<BillDraft> = emptyList(),
    val isLoading: Boolean = true,
    val isNavigating: Boolean = false,
    val saveStatus: DraftSaveStatus = DraftSaveStatus.Saved,
    val error: String? = null
) {
    val readyToSend: Boolean get() = draft?.isValid == true &&
        saveStatus == DraftSaveStatus.Saved && !isLoading && !isNavigating
}

class BillDraftViewModel(
    private val contractId: Long,
    private val store: BillDraftStore,
    initialMonth: YearMonth = YearMonth.now()
) : ViewModel() {
    private val _uiState = MutableStateFlow(BillDraftUiState(initialMonth))
    val uiState = _uiState.asStateFlow()
    private val writes = Mutex()
    private var debounce: Job? = null
    private var revision = 0L
    private var savedRevision = 0L

    init {
        viewModelScope.launch { writes.withLock { load(initialMonth) } }
    }

    fun changeRent(input: String) = edit { it.copy(rentInput = input) }

    fun changeService(serviceId: Long, input: String) = edit { draft ->
        draft.copy(services = draft.services.map {
            if (it.serviceId == serviceId) it.copy(amountInput = input) else it
        })
    }

    private fun edit(transform: (BillDraft) -> BillDraft) {
        val state = _uiState.value
        if (state.isLoading || state.isNavigating) return
        val original = state.draft ?: return
        val draft = transform(original)
        if (draft == original) return
        revision++
        _uiState.value = state.copy(draft = draft, saveStatus = DraftSaveStatus.Unsaved, error = null)
        debounce?.cancel()
        debounce = viewModelScope.launch {
            delay(500.milliseconds)
            // A new edit cancels only the timer, never a database write already in progress.
            savePending()
        }
    }

    fun savePending() {
        debounce?.cancel()
        debounce = null
        viewModelScope.launch { writes.withLock { flushLocked() } }
    }

    fun retry() {
        if (_uiState.value.isLoading || _uiState.value.isNavigating) return
        if (_uiState.value.draft == null) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            viewModelScope.launch { writes.withLock { load(_uiState.value.month) } }
        } else savePending()
    }

    fun selectMonth(month: YearMonth) {
        val state = _uiState.value
        if (state.isLoading || state.isNavigating || month == state.month || state.bills.none { it.month == month }) return
        _uiState.value = state.copy(isNavigating = true)
        debounce?.cancel()
        viewModelScope.launch {
            writes.withLock {
                if (flushLocked()) {
                    val current = _uiState.value
                    _uiState.value = current.copy(month = month, draft = current.bills.first { it.month == month })
                }
                _uiState.value = _uiState.value.copy(isNavigating = false)
            }
        }
    }

    fun leave(onSaved: () -> Unit) {
        if (_uiState.value.isNavigating) return
        _uiState.value = _uiState.value.copy(isNavigating = true)
        debounce?.cancel()
        viewModelScope.launch {
            writes.withLock {
                if (flushLocked()) onSaved()
                _uiState.value = _uiState.value.copy(isNavigating = false)
            }
        }
    }

    private suspend fun load(month: YearMonth) {
        _uiState.value = _uiState.value.copy(month = month, draft = null, isLoading = true, error = null)
        try {
            val bills = store.list(contractId)
            val draft = bills.firstOrNull { it.month >= month } ?: bills.lastOrNull()
            checkNotNull(draft) { "No bills for this contract" }
            revision = 0
            savedRevision = 0
            _uiState.value = _uiState.value.copy(month = draft.month, draft = draft, bills = bills, isLoading = false, saveStatus = DraftSaveStatus.Saved)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Could not open this bill. Please retry.")
        }
    }

    // Call only with writes held. A newer edit remains dirty until its own write succeeds.
    private suspend fun flushLocked(): Boolean {
        while (revision != savedRevision) {
            val draft = _uiState.value.draft ?: return true
            val savingRevision = revision
            _uiState.value = _uiState.value.copy(saveStatus = DraftSaveStatus.Saving, error = null)
            try {
                val saved = store.save(draft)
                savedRevision = savingRevision
                val current = _uiState.value
                _uiState.value = current.copy(
                    draft = current.draft?.copy(updatedAt = saved.updatedAt),
                    bills = current.bills.map { if (it.id == saved.id) saved else it },
                    saveStatus = if (revision == savedRevision) DraftSaveStatus.Saved else DraftSaveStatus.Unsaved
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    saveStatus = DraftSaveStatus.Error,
                    error = "Changes could not be saved. Your edits are still here. Please retry."
                )
                return false
            }
        }
        return true
    }
}
