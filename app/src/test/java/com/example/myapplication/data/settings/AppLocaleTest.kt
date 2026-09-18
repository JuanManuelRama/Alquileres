package com.example.myapplication.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Currency
import java.util.Locale

class AppLocaleTest {
    @Test fun switchingLanguagesPreservesDeviceCountryAndCurrency() {
        for ((region, currency) in listOf("AR" to "ARS", "US" to "USD", "GB" to "GBP", "ES" to "EUR")) {
            val device = Locale.forLanguageTag("es-$region")
            for (language in AppLanguage.entries) {
                val result = appLocale(language.locale, device)
                assertEquals(language.locale.language, result.language)
                assertEquals(region, result.country)
                assertEquals(currency, Currency.getInstance(result).currencyCode)
            }
        }
    }

    @Test fun keepsAnExplicitDeviceCurrencyPreference() {
        val device = Locale.forLanguageTag("es-AR-u-cu-usd")
        val result = appLocale(Locale.ENGLISH, device)
        assertEquals("AR", result.country)
        assertEquals("USD", Currency.getInstance(result).currencyCode)
    }
}
