package com.example.myapplication.ui.util

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.myapplication.data.settings.AppLanguage
import com.example.myapplication.data.settings.appLocale
import com.example.myapplication.ui.components.form.Form
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class NumberFormattersLocaleTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun languageChangesRefreshSeparatorsWithoutChangingInputOrGlobalLocale() {
        var language by mutableStateOf(AppLanguage.ENGLISH)
        val deviceLocale = Locale.forLanguageTag("es-AR")
        var input by mutableStateOf("1234.50")
        val defaultLocale = Locale.getDefault()
        composeRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(appLocale(language.locale, deviceLocale))
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                val formatter = rememberNumberVisualTransformation()
                MaterialTheme {
                    Form(submitText = "Save", onSubmit = {}, showActions = false) {
                        Item(
                            field = "Amount",
                            value = input,
                            onValueChange = { input = it },
                            visualTransformation = formatter,
                        )
                    }
                }
            }
        }
        composeRule.onNodeWithText("1,234.50").assertExists()
        composeRule.runOnIdle { language = AppLanguage.SPANISH }
        composeRule.onNodeWithText("1.234,50").assertExists()
        composeRule.onNodeWithText("1,234.50").assertDoesNotExist()
        composeRule.runOnIdle { language = AppLanguage.ENGLISH }
        composeRule.onNodeWithText("1,234.50").assertExists()
        assertEquals(defaultLocale, Locale.getDefault())
        assertEquals("1234.50", input)
    }
}
