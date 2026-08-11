// CreatePropiedadForm.kt
package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.model.Direccion
import com.example.myapplication.model.Propiedad
import com.example.myapplication.model.Propietario
import com.example.myapplication.ui.components.form.Form
import com.example.myapplication.ui.components.form.FormFieldKey
import com.example.myapplication.ui.components.form.TypeaheadOption


@Composable
fun AddPropiedadScreen(
    existingPropiedades: List<Propiedad>,
    onSubmit: (Propiedad) -> Unit,
    modifier: Modifier = Modifier
) {
    // Keys: created once per composition, reused as identity across Item + autoFill
    val propiedadKey = remember { FormFieldKey("Propiedad") }
    val calleKey = remember { FormFieldKey("Calle") }
    val alturaKey = remember { FormFieldKey("Altura") }
    val pisoKey = remember { FormFieldKey("Piso") }
    val departamentoKey = remember { FormFieldKey("Departamento") }
    val propietarioKey = remember { FormFieldKey("Propietario") }

    var propiedadQuery by remember { mutableStateOf("") }
    var calle by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var piso by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }
    var propietario by remember { mutableStateOf("") }

    val propiedadOptions = remember(existingPropiedades) {
        existingPropiedades.map { propiedad ->
            TypeaheadOption(
                value = propiedad.direccion.toString(),
                autoFill = mapOf(
                    calleKey to propiedad.direccion.calle,
                    alturaKey to propiedad.direccion.altura,
                    pisoKey to (propiedad.direccion.piso ?: ""),
                    departamentoKey to (propiedad.direccion.departamento ?: ""),
                    propietarioKey to propiedad.propietario.nombre
                )
            )
        }
    }

    Scaffold { padding ->
        Form(
            submitText = "Crear propiedad",
            modifier = modifier.padding(padding),
            onSubmit = {
                onSubmit(
                    Propiedad(
                        id = 0, // assigned by backend/repo
                        direccion = Direccion(
                            calle = calle,
                            altura = altura,
                            piso = piso.ifBlank { null },
                            departamento = departamento.ifBlank { null }
                        ),
                        propietario = Propietario(propietario)
                    )
                )
            }
        ) {
            TypeaheadItem(
                key = propiedadKey,
                value = propiedadQuery,
                options = propiedadOptions,
                mandatory = true,
                onValueChange = { propiedadQuery = it },
                onCreateNew = { propiedadQuery = it } // new address, fields stay editable/manual
            )
            Item(key = calleKey, value = calle, mandatory = true, onValueChange = { calle = it })
            Item(key = alturaKey, value = altura, mandatory = true, keyboardType = KeyboardType.Number, onValueChange = { altura = it })
            Item(key = pisoKey, value = piso, onValueChange = { piso = it })
            Item(key = departamentoKey, value = departamento, onValueChange = { departamento = it })
            Item(key = propietarioKey, value = propietario, mandatory = true, onValueChange = { propietario = it })
        }
    }
}