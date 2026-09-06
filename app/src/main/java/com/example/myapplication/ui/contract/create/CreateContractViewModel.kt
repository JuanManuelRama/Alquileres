package com.example.myapplication.ui.contract.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.data.repository.ServiceRepository
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreateContractViewModel(
    private val propertyRepository: PropertyRepository,
    private val ownerRepository: OwnerRepository,
    private val serviceRepository: ServiceRepository,
    private val contractRepository: ContractRepository
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

    val contracts: StateFlow<List<Contract>> = contractRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addProperty(property: Property) {
        viewModelScope.launch {
            val owner = if (property.owner.id == 0L) {
                ownerRepository.insert(property.owner)
            } else {
                property.owner
            }
            propertyRepository.insert(property.copy(owner = owner))
        }
    }

    fun saveContract(contract: Contract) {
        viewModelScope.launch {
            val property = if (contract.property.id == 0L) {
                val owner = if (contract.property.owner.id == 0L) {
                    ownerRepository.insert(contract.property.owner)
                } else {
                    contract.property.owner
                }
                propertyRepository.insert(contract.property.copy(owner = owner))
            } else {
                contract.property
            }

            val services = contract.services.map { service ->
                if (service.id == 0L) serviceRepository.insert(service) else service
            }

            contractRepository.insert(
                contract.copy(
                    property = property,
                    services = services
                )
            )
        }
    }
}
