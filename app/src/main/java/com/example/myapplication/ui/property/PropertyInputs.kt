package com.example.myapplication.ui.property

import com.example.myapplication.model.Owner
import com.example.myapplication.ui.components.picker.PickerInputs
import com.example.myapplication.ui.owner.OwnerInputs
import kotlinx.serialization.Serializable

@Serializable
data class PropertyInputs(
    val street: String = "",
    val number: String = "",
    val unit: String = "",
    val selectedOwner: Owner? = null,
    val ownerPicker: PickerInputs = PickerInputs(),
    val ownerDraft: OwnerInputs = OwnerInputs()
)
