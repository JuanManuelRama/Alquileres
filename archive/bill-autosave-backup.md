# Bill autosave backup

Original implementation before switching to explicit Form submission. Reference only; not compiled.

## app/src/main/java/com/example/myapplication/ui/contract/bill/BillDraftViewModel.kt

```kotlin
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

```

## app/src/main/java/com/example/myapplication/ui/contract/bill/BillDraftScreen.kt

```kotlin
package com.example.myapplication.ui.contract.bill

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import com.example.myapplication.model.BillDraft
import java.time.YearMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDraftScreen(viewModel: BillDraftViewModel, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.savePending()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    BackHandler { viewModel.leave(onBackClick) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Prepare bill") }, navigationIcon = {
                IconButton(onClick = { viewModel.leave(onBackClick) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().imePadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator()
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::retry, enabled = !state.isNavigating) { Text("Retry") }
            }
            val draft = state.draft
            if (draft != null) {
                BillCards(state.bills, state.month, !state.isNavigating, viewModel::selectMonth) {
                    Text("Draft bill - does not record a payment.", style = MaterialTheme.typography.bodySmall)
                    Text(when (state.saveStatus) {
                        DraftSaveStatus.Saved -> "Saved"
                        DraftSaveStatus.Unsaved -> "Unsaved changes"
                        DraftSaveStatus.Saving -> "Saving..."
                        DraftSaveStatus.Error -> "Not saved"
                    })
                    BillAmountField(
                        "Rent", draft.rentInput, viewModel::changeRent,
                        enabled = !state.isNavigating,
                        invalid = draft.rentInput.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } != true,
                        error = "Enter a positive rent amount"
                    )
                    Text("Services", style = MaterialTheme.typography.titleMedium)
                    if (draft.services.isEmpty()) Text("No services on this bill.")
                    draft.services.forEach { service ->
                        key(draft.id, service.serviceId) {
                            BillAmountField(
                                service.name, service.amountInput,
                                { viewModel.changeService(service.serviceId, it) },
                                enabled = !state.isNavigating,
                                invalid = service.amountInput.toBigDecimalOrNull() == null,
                                error = "Enter a service amount (zero is allowed)"
                            )
                        }
                    }
                    draft.total?.let { Text("Total: $${it.toPlainString()}", style = MaterialTheme.typography.titleLarge) }
                    Text(
                        if (state.readyToSend) "Ready to send"
                        else if (draft.isValid) "Waiting for changes to be saved"
                        else "Complete all amounts to make this bill ready to send.",
                        color = if (state.readyToSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BillAmountField(
    label: String, value: String, onValueChange: (String) -> Unit,
    enabled: Boolean, invalid: Boolean, error: String
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = Modifier.fillMaxWidth(), enabled = enabled, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = invalid && value.isNotEmpty(),
        supportingText = { if (invalid) Text(error) }
    )
}

/** Displays a finite collection of bill cards supplied by the caller. */
@Composable
fun BillCards(
    bills: List<BillDraft>, selectedMonth: YearMonth, enabled: Boolean,
    onSelect: (YearMonth) -> Unit, content: @Composable () -> Unit
) {
    val index = bills.indexOfFirst { it.month == selectedMonth }
    if (index < 0) return
    var showBillPicker by remember { mutableStateOf(false) }
    val monthFormat = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    if (showBillPicker && enabled) {
        AlertDialog(
            onDismissRequest = { showBillPicker = false },
            title = { Text("Select bill") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    state = rememberLazyListState(initialFirstVisibleItemIndex = index)
                ) {
                    items(bills, key = { it.id }) { bill ->
                        TextButton(
                            onClick = {
                                showBillPicker = false
                                onSelect(bill.month)
                            },
                            modifier = Modifier.fillMaxWidth().semantics {
                                selected = bill.month == selectedMonth
                            }
                        ) {
                            Text(
                                bill.month.format(monthFormat) +
                                    if (bill.month == selectedMonth) " (current)" else ""
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBillPicker = false }) { Text("Cancel") }
            }
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onSelect(bills[index - 1].month) }, enabled = enabled && index > 0) { Text("Previous") }
            TextButton(
                onClick = { showBillPicker = true },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) { Text(selectedMonth.format(monthFormat)) }
            TextButton(onClick = { onSelect(bills[index + 1].month) }, enabled = enabled && index < bills.lastIndex) { Text("Next") }
        }
        key(bills[index].id) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    content()
                }
            }
        }
    }
}

```

## app/src/test/java/com/example/myapplication/ui/contract/bill/BillDraftViewModelTest.kt

```kotlin
package com.example.myapplication.ui.contract.bill

import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.BillDraftStore
import com.example.myapplication.model.BillDraft
import com.example.myapplication.model.BillDraftService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.YearMonth
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class BillDraftViewModelTest {
    private val month = YearMonth.of(2026, 12)
    private val store = FakeStore()
    private val models = mutableListOf<BillDraftViewModel>()

    @Before fun setup() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun teardown() {
        models.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
    }
    private fun model(id: Long = 1) = BillDraftViewModel(id, store, month).also { models.add(it) }

    @Test fun autosaveWaitsForPauseAndPreservesPartialInputs() = runTest {
        val vm = model()
        assertTrue(vm.uiState.value.isLoading)
        runCurrent()
        assertFalse(vm.uiState.value.readyToSend)
        vm.changeService(7, "12.")
        advanceTimeBy(499.milliseconds)
        runCurrent()
        assertTrue(store.writes.isEmpty())
        vm.changeService(7, "-")
        advanceTimeBy(500.milliseconds)
        runCurrent()
        assertEquals("-", store.writes.single().services.single().amountInput)
        assertEquals(DraftSaveStatus.Saved, vm.uiState.value.saveStatus)
        assertFalse(vm.uiState.value.readyToSend)
        val reopened = model()
        runCurrent()
        assertEquals("-", reopened.uiState.value.draft!!.services.single().amountInput)
    }

    @Test fun readyRequiresValidAmountsAndSuccessfulSaveIncludingZeroServices() = runTest {
        val vm = model()
        runCurrent()
        vm.changeService(7, "0")
        assertFalse(vm.uiState.value.readyToSend)
        vm.savePending()
        runCurrent()
        assertTrue(vm.uiState.value.readyToSend)
        vm.changeRent("0")
        vm.savePending()
        runCurrent()
        assertFalse(vm.uiState.value.readyToSend)
        vm.changeRent("1200")
        vm.changeService(7, "")
        vm.savePending()
        runCurrent()
        assertFalse(vm.uiState.value.readyToSend)
    }

    @Test fun emptyServiceListNeedsOnlyValidSavedRent() = runTest {
        store.withServices = false
        val vm = model()
        runCurrent()
        assertTrue(vm.uiState.value.readyToSend)
    }

    @Test fun switchingMonthFlushesAndReopeningRestoresEachMonthsValues() = runTest {
        val vm = model()
        runCurrent()
        vm.changeRent("1234")
        vm.selectMonth(month.plusMonths(1))
        runCurrent()
        assertEquals("1234", store.records.getValue(1L to month).rentInput)
        assertEquals(month.plusMonths(1), vm.uiState.value.month)
        vm.changeRent("2345")
        vm.selectMonth(month)
        runCurrent()
        assertEquals("1234", vm.uiState.value.draft!!.rentInput)
        assertEquals("2345", store.records.getValue(1L to month.plusMonths(1)).rentInput)
        val other = model(2)
        runCurrent()
        assertEquals("1000", other.uiState.value.draft!!.rentInput)
    }

    @Test fun failedSaveRetainsEditsBlocksLeavingAndCanBeRetried() = runTest {
        val vm = model()
        runCurrent()
        store.failSave = true
        vm.changeRent("1500")
        var left = false
        vm.leave { left = true }
        runCurrent()
        assertFalse(left)
        assertEquals(DraftSaveStatus.Error, vm.uiState.value.saveStatus)
        assertEquals("1500", vm.uiState.value.draft!!.rentInput)
        vm.selectMonth(month.plusMonths(1))
        runCurrent()
        assertEquals(month, vm.uiState.value.month)
        store.failSave = false
        vm.retry()
        runCurrent()
        assertEquals(DraftSaveStatus.Saved, vm.uiState.value.saveStatus)
        vm.leave { left = true }
        runCurrent()
        assertTrue(left)
    }

    @Test fun editsDuringSlowSaveAreWrittenAfterItAndBackWaitsForLatest() = runTest {
        val vm = model()
        runCurrent()
        val gate = CompletableDeferred<Unit>()
        store.gate = gate
        vm.changeRent("1100")
        advanceTimeBy(500.milliseconds)
        runCurrent()
        assertEquals(DraftSaveStatus.Saving, vm.uiState.value.saveStatus)
        vm.changeRent("2200")
        var left = false
        vm.leave { left = true }
        runCurrent()
        assertFalse(left)
        gate.complete(Unit)
        runCurrent()
        assertTrue(left)
        assertEquals(listOf("1100", "2200"), store.writes.map { it.rentInput })
        assertEquals("2200", store.records.getValue(1L to month).rentInput)
    }

    @Test fun backgroundFlushDoesNotWaitForDebounce() = runTest {
        val vm = model()
        runCurrent()
        vm.changeRent("1800")
        vm.savePending()
        runCurrent()
        assertEquals("1800", store.writes.single().rentInput)
    }

    @Test fun failedLoadCanBeRetried() = runTest {
        store.failLoad = true
        val vm = model()
        runCurrent()
        assertNull(vm.uiState.value.draft)
        assertNotNull(vm.uiState.value.error)
        store.failLoad = false
        vm.retry()
        runCurrent()
        assertNotNull(vm.uiState.value.draft)
        assertNull(vm.uiState.value.error)
    }

    @Test fun navigationStaysWithinPreloadedBills() = runTest {
        val vm = model()
        runCurrent()
        val ids = vm.uiState.value.bills.map { it.id }
        vm.selectMonth(month.minusMonths(1))
        runCurrent()
        assertEquals(month, vm.uiState.value.month)
        vm.selectMonth(month.plusMonths(2))
        runCurrent()
        vm.selectMonth(month.plusMonths(3))
        runCurrent()
        assertEquals(month.plusMonths(2), vm.uiState.value.month)
        assertEquals(ids, vm.uiState.value.bills.map { it.id })
        assertEquals(3, store.records.size)
    }

    private class FakeStore : BillDraftStore {
        val records = mutableMapOf<Pair<Long, YearMonth>, BillDraft>()
        val writes = mutableListOf<BillDraft>()
        var gate: CompletableDeferred<Unit>? = null
        var failSave = false
        var failLoad = false
        var withServices = true
        override suspend fun list(contractId: Long): List<BillDraft> =
            (0L..2L).map { open(contractId, YearMonth.of(2026, 12).plusMonths(it)) }

        override suspend fun open(contractId: Long, month: YearMonth): BillDraft {
            check(!failLoad)
            return records.getOrPut(contractId to month) {
                BillDraft(records.size.toLong() + 1, contractId, month, "1000",
                    if (withServices) listOf(BillDraftService(7, "Water")) else emptyList(), 1)
            }
        }
        override suspend fun save(draft: BillDraft): BillDraft {
            gate?.await()
            check(!failSave)
            writes.add(draft)
            return draft.copy(updatedAt = draft.updatedAt + 1).also { records[draft.contractId to draft.month] = it }
        }
    }
}

```
