package com.example.myapplication.ui.contract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.PaymentRepository
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Payment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContractViewModel(
    private val contractRepository: ContractRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    val contracts: StateFlow<List<Contract>> = contractRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun paymentsFor(contract: Contract): Flow<List<Payment>> =
        paymentRepository.getPaymentsForContract(contract)

    fun savePayment(payment: Payment) {
        viewModelScope.launch {
            paymentRepository.save(payment)
        }
    }
}
