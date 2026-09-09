package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.ui.contract.detail.ContractDetailViewModel
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PaymentRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.data.repository.ServiceRepository
import com.example.myapplication.ui.contract.ContractViewModel
import com.example.myapplication.ui.contract.create.CreateContractViewModel
import com.example.myapplication.data.repository.BillDraftStore
import com.example.myapplication.ui.contract.bill.BillDraftViewModel

class AppViewModelFactory(
    private val propertyRepository: PropertyRepository,
    private val ownerRepository: OwnerRepository,
    private val serviceRepository: ServiceRepository,
    private val contractRepository: ContractRepository,
    private val paymentRepository: PaymentRepository,
    private val billDraftStore: BillDraftStore
) : ViewModelProvider.Factory {
    fun billDraftFactory(contractId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer { BillDraftViewModel(contractId, billDraftStore) }
    }

    fun contractDetailFactory(contractId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ContractDetailViewModel(contractId, contractRepository, paymentRepository)
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
