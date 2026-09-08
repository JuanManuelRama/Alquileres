package com.example.myapplication.ui.contract.list

sealed interface ContractListUiState {
    data object Loading : ContractListUiState
    data class Loaded(val contracts: List<ContractListItem>) : ContractListUiState
}
