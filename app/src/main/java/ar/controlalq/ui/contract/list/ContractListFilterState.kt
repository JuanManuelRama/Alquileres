package ar.controlalq.ui.contract.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import ar.controlalq.model.Contract
import java.time.YearMonth

internal class ContractListFilterState(
    query: String = "",
    ownerId: Long? = null,
    startingMonth: YearMonth? = null,
    finishingMonth: YearMonth? = null,
    paymentDue: Boolean = false,
    expiresSoon: Boolean = false,
    hikeThisMonth: Boolean = false,
    billIncomplete: Boolean = false,
) {
    var query by mutableStateOf(query)
    var ownerId by mutableStateOf(ownerId)
    var startingMonth by mutableStateOf(startingMonth)
    var finishingMonth by mutableStateOf(finishingMonth)
    var paymentDue by mutableStateOf(paymentDue)
    var expiresSoon by mutableStateOf(expiresSoon)
    var hikeThisMonth by mutableStateOf(hikeThisMonth)
    var billIncomplete by mutableStateOf(billIncomplete)

    val hasActiveFilters: Boolean get() = query.isNotBlank() || ownerId != null ||
        startingMonth != null || finishingMonth != null || paymentDue || expiresSoon || hikeThisMonth || billIncomplete

    fun clear() {
        query = ""
        ownerId = null
        startingMonth = null
        finishingMonth = null
        paymentDue = false
        expiresSoon = false
        hikeThisMonth = false
        billIncomplete = false
    }

    fun matches(contract: Contract, status: ContractListItem? = null): Boolean =
        contract.property.toString().contains(query.trim(), ignoreCase = true) &&
            (ownerId == null || contract.property.owner.id == ownerId) &&
            (startingMonth == null || contract.finishingMonth >= startingMonth) &&
            (finishingMonth == null || contract.startingMonth <= finishingMonth) &&
            (!paymentDue || status?.paymentDue == true) &&
            (!expiresSoon || status?.expiresSoon == true) &&
            (!hikeThisMonth || status?.hikeThisMonth == true) &&
            (!billIncomplete || status?.billIncomplete == true)

    companion object {
        val Saver = listSaver<ContractListFilterState, String>(
            save = { listOf(it.query, it.ownerId?.toString().orEmpty(),
                it.startingMonth?.toString().orEmpty(), it.finishingMonth?.toString().orEmpty(),
                it.paymentDue.toString(), it.expiresSoon.toString(), it.hikeThisMonth.toString(), it.billIncomplete.toString()) },
            restore = {
                ContractListFilterState(it[0], it[1].toLongOrNull(),
                    it[2].takeIf(String::isNotEmpty)?.let(YearMonth::parse),
                    it[3].takeIf(String::isNotEmpty)?.let(YearMonth::parse),
                    it[4].toBoolean(), it[5].toBoolean(), it[6].toBoolean(), it.getOrNull(7).toBoolean())
            }
        )
    }
}
