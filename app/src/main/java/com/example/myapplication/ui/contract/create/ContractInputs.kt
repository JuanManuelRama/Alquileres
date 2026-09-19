package com.example.myapplication.ui.contract.create

import com.example.myapplication.model.Contract
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.model.YearMonthSerializer
import com.example.myapplication.ui.components.picker.PickerInputs
import com.example.myapplication.ui.property.PropertyInputs
import com.example.myapplication.ui.service.ServiceInputs
import kotlinx.serialization.Serializable
import java.math.RoundingMode
import java.time.YearMonth
import java.time.temporal.ChronoUnit

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

fun Contract.renewalInputs(): ContractInputs {
    val start = finishingMonth.plusMonths(1)
    val duration = ChronoUnit.MONTHS.between(startingMonth, finishingMonth)
    val months = adjustments.map { it.time }.sorted()
    val intervals = (listOf(startingMonth) + months).zipWithNext { from, to ->
        ChronoUnit.MONTHS.between(from, to)
    }
    val interval = intervals.distinct().singleOrNull()?.takeIf { it > 0 }
    // Only uniform, known rates can prefill a fixed hike for the new term.
    val rates = adjustments.map { it.amount?.stripTrailingZeros() }.distinct()
    val fixedRate = if (interval != null) rates.singleOrNull() else null
    return ContractInputs(
        tenantName = tenant.name,
        tenantPhone = tenant.phone,
        selectedProperty = property,
        selectedServices = services,
        rent = valueAtMonth(finishingMonth).setScale(2, RoundingMode.HALF_UP).toPlainString(),
        startMonth = start,
        endMonth = start.plusMonths(duration),
        adjustmentTime = interval?.toString().orEmpty(),
        fixedHike = fixedRate?.movePointRight(2)?.toPlainString().orEmpty()
    )
}
