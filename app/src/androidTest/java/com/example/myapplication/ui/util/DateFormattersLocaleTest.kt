package com.example.myapplication.ui.util

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.util.Locale

class DateFormattersLocaleTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun configurationChangesRefreshEveryFormatWithoutRecreatingTheScreen() {
        var locale by mutableStateOf(Locale.US)
        val month = YearMonth.of(2026, 1)
        val date = LocalDate.of(2026, 1, 15)
        fun expected(locale: Locale) = DateFormatters(locale).let {
            listOf(it.month(month), it.shortMonth(month), it.date(date), it.shortDate(date), it.monthName(Month.JANUARY)).joinToString(" | ")
        }
        composeRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply { setLocale(locale) }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                val dates = rememberDateFormatters()
                Text(listOf(dates.month(month), dates.shortMonth(month), dates.date(date), dates.shortDate(date), dates.monthName(Month.JANUARY)).joinToString(" | "))
            }
        }
        composeRule.onNodeWithText(expected(Locale.US)).assertExists()
        val spanish = Locale.forLanguageTag("es-ES")
        composeRule.runOnIdle { locale = spanish }
        composeRule.onNodeWithText(expected(spanish)).assertExists()
        composeRule.onNodeWithText(expected(Locale.US)).assertDoesNotExist()
        composeRule.runOnIdle { locale = Locale.US }
        composeRule.onNodeWithText(expected(Locale.US)).assertExists()
    }
}
