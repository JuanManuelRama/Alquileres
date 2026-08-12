package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Property
import com.example.myapplication.model.Tenant
import com.example.myapplication.ui.picker.PropertyPicker
import com.example.myapplication.ui.picker.TenantPicker

@Composable
fun CreateContractScreen(
    tenants: List<Tenant>,
    properties: List<Property>
    //onCreateContract: (ContractDraft) -> Unit,
) {
    var selectedTenant by remember { mutableStateOf<Tenant?>(null) }
    var selectedProperty by remember { mutableStateOf<Property?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "New rental contract",
            style = MaterialTheme.typography.headlineSmall
        )

        PropertyPicker(
            properties = properties,
            selectedProperty = selectedProperty,
            onPropertySelected = { selectedProperty = it },
            onPropertyCreated = { selectedProperty = it },
            modifier = Modifier
        )

        TenantPicker(
            tenants = tenants,
            selectedTenant = selectedTenant,
            onTenantSelected = { selectedTenant = it },
            onTenantCreated = { selectedTenant = it },
            modifier = Modifier
        )

        // Other contract fields...

        Button(
            onClick = { -> }
                /*selectedTenant?.let { tenant ->
                    onCreateContract(
                        ContractDraft(
                            tenantId = tenant.id,
                            // ...
                        )
                    )
                }
            }*/,
            enabled = selectedTenant != null
        ) {
            Text("Create contract")
        }
    }
}