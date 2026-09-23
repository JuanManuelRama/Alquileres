package com.example.myapplication.ui.util

import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/** Number formatting only: currency and translated text retain their own locale. */
val LocalNumberLocale = compositionLocalWithComputedDefaultOf<Locale> {
    LocalConfiguration.currentValue.locales[0]
}
