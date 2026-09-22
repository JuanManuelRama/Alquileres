package com.example.myapplication.ui.property

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.ui.SaveStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface PropertyListState {
    data object Loading : PropertyListState
    data object Failed : PropertyListState
    data class Ready(val rows: List<PropertyListItem>, val owners: List<Owner>) : PropertyListState
}

class PropertyListViewModel(
    private val repository: PropertyRepository,
    private val ownerRepository: OwnerRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<PropertyListState>(PropertyListState.Loading)
    val state = _state.asStateFlow()
    private val _saveStatus = MutableStateFlow(SaveStatus.Idle)
    val saveStatus = _saveStatus.asStateFlow()
    private var observation: Job? = null

    init { retry() }

    fun retry() {
        observation?.cancel()
        _state.value = PropertyListState.Loading
        observation = viewModelScope.launch {
            try {
                combine(repository.observeManagement(), ownerRepository.getAll()) { rows, owners ->
                    PropertyListState.Ready(
                        rows.map { it.details.toDomain().listItem(it.isActive, it.hasActiveContracts) },
                        owners,
                    )
                }.collect { _state.value = it }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.value = PropertyListState.Failed
            }
        }
    }

    fun resetStatus() { if (_saveStatus.value != SaveStatus.Saving) _saveStatus.value = SaveStatus.Idle }
    fun save(property: Property) = mutate { repository.update(property) }
    fun setActive(id: Long, active: Boolean) = mutate { repository.setActive(id, active) }
    fun delete(id: Long) = mutate { repository.delete(id) }

    private fun mutate(block: suspend () -> Unit) {
        if (_saveStatus.value == SaveStatus.Saving || _saveStatus.value == SaveStatus.Saved) return
        _saveStatus.value = SaveStatus.Saving
        viewModelScope.launch {
            try {
                block()
                _saveStatus.value = SaveStatus.Saved
            } catch (error: CancellationException) {
                _saveStatus.value = SaveStatus.Idle
                throw error
            } catch (_: Exception) {
                _saveStatus.value = SaveStatus.Failed
            }
        }
    }
}
