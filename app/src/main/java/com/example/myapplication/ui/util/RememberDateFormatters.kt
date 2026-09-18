package com.example.myapplication.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

/** Observe the app's localized configuration instead of the process-wide default locale. */
@Composable
fun rememberDateFormatters(): DateFormatters {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { DateFormatters(locale) }
}
