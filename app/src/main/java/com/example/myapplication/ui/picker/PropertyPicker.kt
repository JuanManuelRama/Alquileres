package com.example.myapplication.ui.picker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myapplication.model.Property
import com.example.myapplication.ui.creator.PropertyCreator

@Composable
fun PropertyPicker(
    properties: List<Property>,
    selectedProperty: Property?,
    onPropertySelected: (Property) -> Unit,
    onPropertyCreated: (Property) -> Unit,
    modifier: Modifier = Modifier,
) {
    EntityPicker(
        items = properties,
        selectedItem = selectedProperty,
        onItemSelected = onPropertySelected,
        onItemCreated = onPropertyCreated,
        itemLabel = { it.toString() },
        createContent = { onCreated, onCanceled ->
            PropertyCreator(
                onCreated = onCreated,
                canceledText = "Cancel",
                onCanceled = onCanceled
            )
        },
        modifier = modifier,
        label = "Property",
        placeholder = "Select property..."
    )
}