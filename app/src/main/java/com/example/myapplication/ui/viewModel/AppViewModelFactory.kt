package com.example.myapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PaymentRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.data.repository.ServiceRepository
import com.example.myapplication.data.repository.TenantRepository

class AppViewModelFactory(
    private val propertyRepository: PropertyRepository,
    private val ownerRepository: OwnerRepository,
    private val tenantRepository: TenantRepository,
    private val serviceRepository: ServiceRepository,
    private val contractRepository: ContractRepository,
    private val paymentRepository: PaymentRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(CreateContractViewModel::class.java) -> {
                CreateContractViewModel(
                    propertyRepository = propertyRepository,
                    ownerRepository = ownerRepository,
                    tenantRepository = tenantRepository,
                    serviceRepository = serviceRepository,
                    contractRepository = contractRepository
                ) as T
            }
            modelClass.isAssignableFrom(ContractViewModel::class.java) -> {
                ContractViewModel(
                    contractRepository = contractRepository,
                    paymentRepository = paymentRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
