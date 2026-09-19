package com.example.myapplication.ui.contract.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ContractWriter
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.data.repository.ServiceRepository
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.ui.SaveStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreateContractViewModel(
    propertyRepository: PropertyRepository,
    ownerRepository: OwnerRepository,
    serviceRepository: ServiceRepository,
    private val contractRepository: ContractWriter
) : ViewModel() {
    val owners: StateFlow<List<Owner>> = ownerRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val properties: StateFlow<List<Property>> = propertyRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val services: StateFlow<List<Service>> = serviceRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _saveState = MutableStateFlow(SaveStatus.Idle)
    val saveState = _saveState.asStateFlow()

    fun saveContract(contract: Contract, renewingContractId: Long? = null) {
        if (_saveState.value == SaveStatus.Saving || _saveState.value == SaveStatus.Saved) return
        _saveState.value = SaveStatus.Saving
        viewModelScope.launch {
            try {
                contractRepository.insert(contract, renewingContractId)
                _saveState.value = SaveStatus.Saved
            } catch (error: CancellationException) {
                _saveState.value = SaveStatus.Idle
                throw error
            } catch (_: Exception) {
                _saveState.value = SaveStatus.Failed
            }
        }
    }
}
