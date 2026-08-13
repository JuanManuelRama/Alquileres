package com.example.myapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.data.repository.TenantRepository

class PropertyViewModelFactory(
    private val propertyRepository: PropertyRepository,
    private val ownerRepository: OwnerRepository,
    private val tenantRepository: TenantRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        @Suppress("UNCHECKED_CAST")
        return CreateContractViewModel(
            propertyRepository = propertyRepository,
            ownerRepository = ownerRepository,
            tenantRepository = tenantRepository
        ) as T
    }
}