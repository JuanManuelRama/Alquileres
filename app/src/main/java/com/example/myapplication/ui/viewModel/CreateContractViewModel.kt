package com.example.myapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.data.repository.TenantRepository
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreateContractViewModel(
    private val propertyRepository: PropertyRepository,
    private val ownerRepository: OwnerRepository,
    private val tenantRepository: TenantRepository
) : ViewModel() {
    fun owners(): StateFlow<List<Owner>> {
        return ownerRepository.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    }

    fun properties(): StateFlow<List<Property>> {
        return propertyRepository.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    }

    fun tenants(): StateFlow<List<Tenant>> {
        return tenantRepository.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    }

    fun addProperty(property: Property) {
        viewModelScope.launch {
            var id = property.owner.id
            if (id == null) {
                id = ownerRepository.insert(property.owner)
            }
            propertyRepository.insert(property, id)
        }
    }
}