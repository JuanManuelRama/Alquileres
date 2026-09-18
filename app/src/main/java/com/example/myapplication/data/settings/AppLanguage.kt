package com.example.myapplication.data.settings

import java.util.Locale

enum class AppLanguage(
    val displayName: String,
    val flag: String,
    val locale: Locale,
) {
    ENGLISH(
        displayName = "English",
        flag = "🇬🇧",
        locale = Locale.ENGLISH,
    ),
    SPANISH(
        displayName = "Español",
        flag = "🇪🇸",
        locale = Locale.forLanguageTag("es"),
    );

    val combined: String = "$flag $displayName"
}
