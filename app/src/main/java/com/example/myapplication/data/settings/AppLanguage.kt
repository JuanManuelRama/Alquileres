package com.example.myapplication.data.settings

enum class AppLanguage(
    val displayName: String,
    val flag: String,
) {
    ENGLISH(
        displayName = "English",
        flag = "🇬🇧",
    ),
    SPANISH(
        displayName = "Español",
        flag = "🇪🇸",
    );

    val combined: String = "$flag $displayName"
}
