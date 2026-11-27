package ar.controlalq.ui.components.picker

import kotlinx.serialization.Serializable

@Serializable
data class PickerInputs(
    val query: String = "",
    val isCreating: Boolean = false
)
