package com.example.myapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository

class PropertyViewModelFactory(
    private val propertyRepository: PropertyRepository,
    private val ownerRepository: OwnerRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        @Suppress("UNCHECKED_CAST")
        return CreateContractViewModel(propertyRepository, ownerRepository) as T
    }
}