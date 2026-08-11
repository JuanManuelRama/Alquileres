package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.myapplication.model.Direccion
import com.example.myapplication.model.Propiedad
import com.example.myapplication.model.Propietario
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val propietario = Propietario(1, "Juan Perez")
        val dptos: List<Propiedad> = listOf(
            Propiedad(
                id = 1,
                direccion = Direccion(1, "Calle", "1111", "3", "D"),
                propietario = propietario
            )
        )

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        dptos = dptos,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}