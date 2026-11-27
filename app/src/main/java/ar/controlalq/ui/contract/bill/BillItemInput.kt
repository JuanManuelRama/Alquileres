package ar.controlalq.ui.contract.bill

import ar.controlalq.model.BillItem
import ar.controlalq.ui.components.form.Validate
import ar.controlalq.ui.components.form.isBigDecimal
import ar.controlalq.ui.components.form.limitDecimals
import ar.controlalq.ui.components.form.maxLen
import ar.controlalq.ui.components.form.noInvalidChars
import ar.controlalq.ui.util.toInputBigDecimalOrNull
import kotlinx.serialization.Serializable
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
