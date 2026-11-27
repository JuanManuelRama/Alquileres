package ar.controlalq.ui.contract.list

import ar.controlalq.model.Contract

sealed interface ContractListUiState {
    val isInactive: Boolean

    data class Loading(override val isInactive: Boolean = false) : ContractListUiState

    data class Active(val contracts: List<ContractListItem>) : ContractListUiState {
        override val isInactive: Boolean get() = false
    }

    data class Inactive(val contracts: List<Contract>) : ContractListUiState {
        override val isInactive: Boolean get() = true
    }
}
