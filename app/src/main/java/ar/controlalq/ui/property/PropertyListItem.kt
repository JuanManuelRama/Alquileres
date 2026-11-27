package ar.controlalq.ui.property

import ar.controlalq.model.Property

data class PropertyListItem(
    val property: Property,
    val isActive: Boolean,
    val hasActiveContracts: Boolean,
)

internal fun Property.listItem(isActive: Boolean, hasActiveContracts: Boolean): PropertyListItem =
    PropertyListItem(property = this, isActive = isActive, hasActiveContracts = hasActiveContracts)
