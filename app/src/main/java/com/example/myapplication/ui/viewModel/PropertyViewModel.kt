package com.example.myapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.model.Property
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PropertyViewModel(
    private val repository: PropertyRepository
) : ViewModel() {

    val properties: StateFlow<List<Property>> =
        repository.getAll()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun addProperty(property: Property) {
        viewModelScope.launch {
            repository.insert(property)
        }
    }
}