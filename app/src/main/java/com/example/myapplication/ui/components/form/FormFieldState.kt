// FormFieldState.kt
package com.example.myapplication.ui.components.form

sealed interface FormFieldState {
    val key: FormFieldKey
    val field: String
    val mandatory: Boolean
    val onValueChange: (String) -> Unit

    fun isValidated(): Boolean
}