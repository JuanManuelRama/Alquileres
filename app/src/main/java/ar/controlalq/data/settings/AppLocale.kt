package ar.controlalq.data.settings

import java.util.Locale

/** Changing the app language must not replace the device's region (and its currency). */
fun appLocale(language: Locale, deviceLocale: Locale): Locale = Locale.Builder()
    .setLocale(deviceLocale)
    .setLanguage(language.language)
    .setScript(language.script)
    .build()
