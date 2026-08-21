package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.data.repository.ServiceRepository
import com.example.myapplication.data.repository.TenantRepository
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModel.CreateContractViewModel
import com.example.myapplication.ui.viewModel.PropertyViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val application = applicationContext as MyApplication

            val createContractViewModel: CreateContractViewModel by viewModels {
                PropertyViewModelFactory(
                    propertyRepository = PropertyRepository(application.database.propertyDao()),
                    ownerRepository = OwnerRepository(application.database.ownerDao()),
                    tenantRepository = TenantRepository(application.database.tenantDao()),
                    serviceRepository = ServiceRepository(application.database.serviceDao()),
                    contractRepository = ContractRepository(application.database.contractDao())
                )
            }

            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        createContractViewModel = createContractViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}