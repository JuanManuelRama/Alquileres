package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.repository.BillStore
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.data.repository.ServiceRepository
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.ui.contract.ContractViewModel
import com.example.myapplication.ui.contract.bill.BillViewModel
import com.example.myapplication.ui.contract.create.CreateContractViewModel
import com.example.myapplication.ui.contract.detail.ContractDetailViewModel
import com.example.myapplication.ui.settings.SettingsViewModel

class AppViewModelFactory(
    private val propertyRepository: PropertyRepository,
    private val ownerRepository: OwnerRepository,
    private val serviceRepository: ServiceRepository,
    private val contractRepository: ContractRepository,
    private val billStore: BillStore,
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    fun billFactory(contractId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer { BillViewModel(contractId, billStore) }
    }

    fun contractDetailFactory(contractId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ContractDetailViewModel(contractId, contractRepository, billStore)
        }
    }

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(CreateContractViewModel::class.java) -> {
                CreateContractViewModel(
                    propertyRepository = propertyRepository,
                    ownerRepository = ownerRepository,
                    serviceRepository = serviceRepository,
                    contractRepository = contractRepository,
                ) as T
            }
            modelClass.isAssignableFrom(ContractViewModel::class.java) -> {
                ContractViewModel(
                    contractRepository = contractRepository,
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    settingsRepository = settingsRepository,
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
