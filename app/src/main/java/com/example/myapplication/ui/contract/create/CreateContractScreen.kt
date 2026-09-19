package com.example.myapplication.ui.contract.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.components.form.DiscardChangesDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContractScreen(
    viewModel: CreateContractViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    afterCreate: () -> Unit,
    defaults: ContractInputs = ContractInputs(),
    renewingContractId: Long? = null,
) {
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }
    val initialInputs by rememberSerializable { mutableStateOf(defaults) }
    var inputs by rememberSerializable { mutableStateOf(initialInputs) }
    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val owners by viewModel.owners.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()

    var confirmingDiscard by rememberSaveable { mutableStateOf(false) }
    val onBack = {
        if (!isSaving) {
            if (inputs != initialInputs) confirmingDiscard = true else onBackClick()
        }
    }
    BackHandler(onBack = onBack)
    if (confirmingDiscard) {
        DiscardChangesDialog(
            onDiscard = {
                confirmingDiscard = false
                onBackClick()
            },
            onKeepEditing = { confirmingDiscard = false }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_new_contract)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (saveFailed) Text(stringResource(R.string.err_could_not_create_contract))
            ContractCreator(
                enabled = !isSaving,
                inputs = inputs,
                onInputsChanged = { if (!isSaving) inputs = it },
                properties = properties,
                owners = owners,
                services = services,
                onCreated = { contract ->
                    if (!isSaving) {
                        isSaving = true
                        saveFailed = false
                        scope.launch {
                            try {
                                viewModel.saveContract(contract, renewingContractId)
                                afterCreate()
                            } catch (error: CancellationException) {
                                throw error
                            } catch (_: Exception) {
                                saveFailed = true
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
