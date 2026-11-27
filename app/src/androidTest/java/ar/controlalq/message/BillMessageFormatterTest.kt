package ar.controlalq.message

import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry
import ar.controlalq.model.Address
import ar.controlalq.model.Bill
import ar.controlalq.model.BillItem
import ar.controlalq.model.Contract
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import ar.controlalq.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

class BillMessageFormatterTest {
    private fun contextFor(locale: Locale) =
        InstrumentationRegistry.getInstrumentation().targetContext.let { context ->
            context.createConfigurationContext(Configuration(context.resources.configuration).apply {
                setLocale(locale)
            })
        }

    private val month = YearMonth.of(2026, 9)
    private val contract = Contract(
        Property(Address("Street", "12", "A"), Owner("Owner", 1), 1),
        Tenant("Ada", "6502530000"), BigDecimal("1000"), month, month,
        emptyList(), emptyList(), 1
    )
    private val bill = Bill(1, contract, month, BigDecimal("1000"), listOf(
        BillItem("Water", BigDecimal("25.50")),
        BillItem("Refund", BigDecimal("-10.25"), true)
    ), 0)

    @Test fun includesTenantMonthItemsAndExactTotal() {
        assertEquals("""
            Hello Ada,

            Bill for September 2026

            Rent: $1,000
            Water: $25.5
            Refund: $-10.25
            Total: $1,015.25
        """.trimIndent() + "\n", BillMessageFormatter.format(contextFor(Locale.US), bill, Locale.US))
    }

    @Test fun paidBillUsesTheSameBillSummary() {
        val context = contextFor(Locale.US)
        assertEquals(
            BillMessageFormatter.format(context, bill, Locale.US),
            BillMessageFormatter.format(context, bill.copy(paidAt = LocalDate.of(2026, 9, 10)), Locale.US)
        )
    }

    @Test fun usesTranslatedResourcesAndSeparateNumberLocale() {
        val locale = Locale.forLanguageTag("es-AR")
        assertEquals("""
            Hola Ada,

            Factura de septiembre 2026

            Alquiler: $1,000
            Water: $25.5
            Refund: $-10.25
            Total: $1,015.25
        """.trimIndent() + "\n", BillMessageFormatter.format(contextFor(locale), bill, locale, Locale.US))
    }

    @Test(expected = NullPointerException::class)
    fun incompleteBillCannotBeFormattedAsComplete() {
        BillMessageFormatter.format(contextFor(Locale.US), bill.copy(items = listOf(BillItem("Water", null))), Locale.US)
    }
}
