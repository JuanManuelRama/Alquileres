package ar.controlalq.ui.property

import ar.controlalq.model.Owner
import ar.controlalq.ui.components.picker.PickerInputs
import ar.controlalq.ui.owner.OwnerInputs
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
