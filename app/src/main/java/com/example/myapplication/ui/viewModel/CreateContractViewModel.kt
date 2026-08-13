package com.example.myapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreateContractViewModel(
    private val propertyRepository: PropertyRepository,
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    val owners: StateFlow<List<Owner>> =
        ownerRepository.getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    val properties: StateFlow<List<Property>> =
        propertyRepository.getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

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