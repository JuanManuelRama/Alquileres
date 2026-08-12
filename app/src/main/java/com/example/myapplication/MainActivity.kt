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
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewModel.PropertyViewModel
import com.example.myapplication.ui.viewModel.PropertyViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val application = applicationContext as MyApplication

            val repository = PropertyRepository(
                application.database.propertyDao()
            )

            val viewModel: PropertyViewModel by viewModels {
                PropertyViewModelFactory(repository)
            }

            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    CreateContractScreen(
//                        properties = listOf(Property(Address("San Jose", "911", null), Owner("pepito"))),
//                        tenants = listOf(Tenant("Juan", "115556"), Tenant("Pedro", "1894156")),
//                    )/*
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}