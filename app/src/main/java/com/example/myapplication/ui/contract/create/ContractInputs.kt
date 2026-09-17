package com.example.myapplication.ui.contract.create

import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.model.YearMonthSerializer
import com.example.myapplication.ui.components.picker.PickerInputs
import com.example.myapplication.ui.property.PropertyInputs
import com.example.myapplication.ui.service.ServiceInputs
import kotlinx.serialization.Serializable
import java.time.YearMonth

@Serializable
data class ContractInputs(
    val tenantName: String = "",
    val tenantPhone: String = "",
    val selectedProperty: Property? = null,
    val rent: String = "",
    val fixedHike: String = "",
    val selectedServices: List<Service> = emptyList(),
    @Serializable(with = YearMonthSerializer::class)
    val startMonth: YearMonth? = null,
    @Serializable(with = YearMonthSerializer::class)
    val endMonth: YearMonth? = null,
    val adjustmentTime: String = "",
    val propertyPicker: PickerInputs = PickerInputs(),
    val propertyDraft: PropertyInputs = PropertyInputs(),
    val servicePicker: PickerInputs = PickerInputs(),
    val serviceDraft: ServiceInputs = ServiceInputs(),
)
