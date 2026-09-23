package com.example.myapplication.data.settings

import java.util.Locale

enum class NumberSeparator {
    SYSTEM,
    COMMA,
    DOT;

    fun locale(deviceLocale: Locale): Locale = when (this) {
        SYSTEM -> deviceLocale
        COMMA -> Locale.GERMANY
        DOT -> Locale.US
    }
}
