package ar.controlalq.ui.contract.create

import ar.controlalq.model.Address
import ar.controlalq.model.Adjustment
import ar.controlalq.model.Contract
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.model.Service
import ar.controlalq.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

class ContractRenewalTest {
    private val contract = Contract(
        property = Property(Address("Main", "12", null), Owner("Owner", 41), 42),
        tenant = Tenant("Tenant", "123456789"),
        rent = BigDecimal("1000"),
        startingMonth = YearMonth.of(2026, 1),
        finishingMonth = YearMonth.of(2026, 12),
        adjustments = listOf(Adjustment(YearMonth.of(2026, 7), BigDecimal("0.10"))),
        services = listOf(Service("Water", 7)),
        id = 99,
    )

    @Test fun renewalPreservesPeopleServicesAndDurationAndUsesFinalRent() {
        val inputs = contract.renewalInputs()
        assertEquals(contract.property, inputs.selectedProperty)
        assertEquals(contract.tenant.name, inputs.tenantName)
        assertEquals(contract.tenant.phone, inputs.tenantPhone)
        assertEquals(contract.services, inputs.selectedServices)
        assertEquals(YearMonth.of(2027, 1), inputs.startMonth)
        assertEquals(YearMonth.of(2027, 12), inputs.endMonth)
        assertEquals("1100.00", inputs.rent)
        assertEquals("6", inputs.adjustmentTime)
        assertEquals(0, BigDecimal.TEN.compareTo(inputs.fixedHike.toBigDecimal()))
    }

    @Test fun variableOrUnknownHikesAreNotCopiedAsFixedHikes() {
        val adjustments = listOf(
            Adjustment(YearMonth.of(2026, 4), BigDecimal("0.10")),
            Adjustment(YearMonth.of(2026, 7), null),
            Adjustment(YearMonth.of(2026, 10), BigDecimal("0.20")),
        )
        val inputs = contract.copy(adjustments = adjustments).renewalInputs()
        assertEquals("3", inputs.adjustmentTime)
        assertEquals("", inputs.fixedHike)
        assertEquals("1320.00", inputs.rent)
    }

    @Test fun singleMonthAndIrregularSchedulesDoNotInventAnInterval() {
        val singleMonth = contract.copy(finishingMonth = contract.startingMonth, adjustments = emptyList()).renewalInputs()
        assertEquals(YearMonth.of(2026, 2), singleMonth.startMonth)
        assertEquals(singleMonth.startMonth, singleMonth.endMonth)
        assertEquals("", singleMonth.adjustmentTime)
        val irregular = contract.copy(adjustments = listOf(
            Adjustment(YearMonth.of(2026, 3), BigDecimal("0.10")),
            Adjustment(YearMonth.of(2026, 8), BigDecimal("0.10")),
        )).renewalInputs()
        assertEquals("", irregular.adjustmentTime)
        assertEquals("", irregular.fixedHike)
    }
}
