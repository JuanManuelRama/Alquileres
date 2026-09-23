package com.example.myapplication.ui.contract.bill

import com.example.myapplication.ui.util.toInputBigDecimalOrNull
import kotlinx.serialization.Serializable
import com.example.myapplication.model.BillItem
import com.example.myapplication.ui.components.form.Validate
import com.example.myapplication.ui.components.form.isBigDecimal
import com.example.myapplication.ui.components.form.limitDecimals
import com.example.myapplication.ui.components.form.maxLen
import com.example.myapplication.ui.components.form.noInvalidChars
import java.util.UUID

@Serializable
data class BillItemInput(
    val description: String,
    val amount: String,
    val isExtra: Boolean,
    val key: String = UUID.randomUUID().toString()
) {
    val isValidForSaving: Boolean get() =
        Validate(description).noInvalidChars().maxLen(50).isValid &&
            (amount.isEmpty() || Validate(amount).maxLen(12).isBigDecimal().limitDecimals(2).isValid)

    fun toBillItem() = BillItem(description.trim(), amount.toInputBigDecimalOrNull(), isExtra)
}
