package com.example.myapplication.ui.contract.create

import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import com.example.myapplication.model.YearMonthSerializer
import kotlinx.serialization.Serializable
import java.time.YearMonth

@Serializable
data class ContractInputs(
    val tenantName: String,
    val tenantPhone: String,
    val selectedProperty: Property?,
    val rent: String,
    val fixedHike: String,
    val selectedServices: List<Service>,
    @Serializable(with = YearMonthSerializer::class)
    val startMonth: YearMonth?,
    @Serializable(with = YearMonthSerializer::class)
    val endMonth: YearMonth?,
    val adjustmentTime: String,
) {
    companion object {
        fun default() = ContractInputs(
            tenantName = "",
            tenantPhone = "",
            selectedProperty = null,
            rent = "",
            fixedHike = "",
            selectedServices = emptyList(),
            startMonth = null,
            endMonth = null,
            adjustmentTime = ""
        )
    }
}
