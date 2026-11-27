package ar.controlalq.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ar.controlalq.data.repository.BillStore
import ar.controlalq.data.repository.ContractRepository
import ar.controlalq.data.repository.OwnerRepository
import ar.controlalq.data.repository.PropertyRepository
import ar.controlalq.data.repository.ServiceRepository
import ar.controlalq.data.repository.SettingsRepository
import ar.controlalq.ui.contract.ContractViewModel
import ar.controlalq.ui.contract.bill.BillViewModel
import ar.controlalq.ui.contract.create.CreateContractViewModel
import ar.controlalq.ui.contract.detail.ContractDetailViewModel
import ar.controlalq.ui.property.PropertyListViewModel
import ar.controlalq.ui.settings.SettingsViewModel

class AppViewModelFactory(
    private val propertyRepository: PropertyRepository,
    private val ownerRepository: OwnerRepository,
    private val serviceRepository: ServiceRepository,
    private val contractRepository: ContractRepository,
    private val billStore: BillStore,
    private val settingsRepository: SettingsRepository,
) : ViewModelProvider.Factory {
    fun billFactory(contractId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer { BillViewModel(
            contractId = contractId,
            store = billStore,
            settingsRepository = settingsRepository
        ) }
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
            modelClass.isAssignableFrom(PropertyListViewModel::class.java) -> {
                PropertyListViewModel(propertyRepository, ownerRepository) as T
            }
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
