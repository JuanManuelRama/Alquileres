package com.example.myapplication.ui.contract.create

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import com.example.myapplication.model.Address
import com.example.myapplication.model.Owner
import com.example.myapplication.model.Property
import com.example.myapplication.model.Service
import java.time.YearMonth

class ContractInputs(
    tenantName: String = "",
    tenantPhone: String = "",
    selectedProperty: Property? = null,
    rent: String = "",
    fixedHike: String = "",
    selectedServices: List<Service> = emptyList(),
    startMonth: YearMonth? = null,
    endMonth: YearMonth? = null,
    adjustmentTime: String = "",
) {
    var tenantName by mutableStateOf(tenantName)
    var tenantPhone by mutableStateOf(tenantPhone)
    var selectedProperty by mutableStateOf(selectedProperty)
    var rent by mutableStateOf(rent)
    var fixedHike by mutableStateOf(fixedHike)
    var selectedServices by mutableStateOf(selectedServices)
    var startMonth by mutableStateOf(startMonth)
    var endMonth by mutableStateOf(endMonth)
    var adjustmentTime by mutableStateOf(adjustmentTime)

    companion object {
        val Saver = Saver<ContractInputs, Bundle>(
            save = { input ->
                Bundle().apply {
                    putString("tenantName", input.tenantName)
                    putString("tenantPhone", input.tenantPhone)
                    putString("rent", input.rent)
                    putString("fixedHike", input.fixedHike)
                    putString("adjustmentTime", input.adjustmentTime)
                    putString("startMonth", input.startMonth?.toString())
                    putString("endMonth", input.endMonth?.toString())
                    input.selectedProperty?.let { property ->
                        putBundle("property", Bundle().apply {
                            putLong("id", property.id)
                            putString("street", property.address.street)
                            putString("number", property.address.number)
                            putString("unit", property.address.unit)
                            putLong("ownerId", property.owner.id)
                            putString("ownerName", property.owner.name)
                        })
                    }
                    putLongArray("serviceIds", input.selectedServices.map { it.id }.toLongArray())
                    putStringArrayList("serviceNames", ArrayList(input.selectedServices.map { it.name }))
                }
            },
            restore = { saved ->
                val ids = saved.getLongArray("serviceIds")!!
                ContractInputs(
                    tenantName = saved.getString("tenantName")!!,
                    tenantPhone = saved.getString("tenantPhone")!!,
                    selectedProperty = saved.getBundle("property")?.let {
                        Property(
                            Address(it.getString("street")!!, it.getString("number")!!, it.getString("unit")),
                            Owner(it.getString("ownerName")!!, it.getLong("ownerId")),
                            it.getLong("id")
                        )
                    },
                    rent = saved.getString("rent")!!,
                    fixedHike = saved.getString("fixedHike")!!,
                    selectedServices = saved.getStringArrayList("serviceNames")!!.mapIndexed { index, name ->
                        Service(name, ids[index])
                    },
                    startMonth = saved.getString("startMonth")?.let(YearMonth::parse),
                    endMonth = saved.getString("endMonth")?.let(YearMonth::parse),
                    adjustmentTime = saved.getString("adjustmentTime")!!
                )
            }
        )
    }
}
