package com.example.myapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.PaymentRepository
import com.example.myapplication.model.Contract
import com.example.myapplication.model.Payment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

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

    private val _selectedContract = MutableStateFlow<Contract?>(null)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val payments: Flow<List<Payment>> = _selectedContract.flatMapLatest { contract ->
        if (contract == null) flowOf(emptyList())
        else paymentRepository.getPaymentsForContract(contract)
    }

    fun selectContract(contract: Contract?) {
        _selectedContract.value = contract
    }

    fun savePayment(payment: Payment) {
        viewModelScope.launch {
            paymentRepository.save(payment)
        }
    }


    fun updateAdjustmentAmount(contract: Contract, time: LocalDate, amount: BigDecimal?) {
        val contractId = contract.id ?: return
        viewModelScope.launch {
            contractRepository.updateAdjustmentAmount(contractId, time, amount)
        }
    }
}
