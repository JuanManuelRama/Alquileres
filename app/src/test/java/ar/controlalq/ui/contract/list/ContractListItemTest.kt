package ar.controlalq.ui.contract.list

import ar.controlalq.model.Address
import ar.controlalq.model.Contract
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.model.Tenant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class ContractListItemTest {
    private val month = YearMonth.of(2026, 12)
    private fun contract() = Contract(
        property = Property(Address("Main", "1", null), Owner("Owner")),
        tenant = Tenant("Tenant", "123"), rent = BigDecimal("1000"),
        startingMonth = YearMonth.of(2026, 1), finishingMonth = YearMonth.of(2027, 1),
        adjustments = emptyList(), services = emptyList(), id = 1L
    )

    @Test
    fun incompleteBillTakesPrecedenceOverPaymentDue() {
        val item = contract().listItem(month, paymentDue = true, hikeThisMonth = true, billIncomplete = true)
        assertTrue(item.billIncomplete)
        assertFalse(item.paymentDue)
        assertTrue(item.hikeThisMonth)
    }

    @Test
    fun expiryIncludesThisAndNextMonthAcrossYearBoundary() {
        assertTrue(contract().listItem(month, false, false).expiresSoon)
        assertTrue(contract().copy(finishingMonth = month).listItem(month, false, false).expiresSoon)
        assertFalse(contract().copy(finishingMonth = month.minusMonths(1)).listItem(month, false, false).expiresSoon)
        assertFalse(contract().copy(finishingMonth = month.plusMonths(2)).listItem(month, false, false).expiresSoon)
    }

}
