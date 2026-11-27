package ar.controlalq.ui.contract.create

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.controlalq.R
import ar.controlalq.ui.contract.detail.ContractDetailUiState
import ar.controlalq.ui.contract.detail.ContractDetailViewModel

@Composable
fun RenewContractScreen(
    detailViewModel: ContractDetailViewModel,
    createViewModel: CreateContractViewModel,
    onBackClick: () -> Unit,
    afterCreate: () -> Unit,
) {
    val state by detailViewModel.uiState.collectAsStateWithLifecycle()
    val ready = state as? ContractDetailUiState.Ready
    val title = stringResource(R.string.action_renew_contract)
    if (ready != null) {
        CreateContractScreen(
            viewModel = createViewModel,
            onBackClick = onBackClick,
            afterCreate = afterCreate,
            defaults = ready.contract.renewalInputs(),
            renewingContractId = ready.contract.id,
        )
    } else {
        Scaffold(topBar = {
            TopAppBar(title = { Text(title) }, navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                }
            })
        }) { padding ->
            Text(
                stringResource(if (state == ContractDetailUiState.Loading) R.string.loading_contract else R.string.err_contract_not_found),
                modifier = Modifier.padding(padding),
            )
        }
    }
}
