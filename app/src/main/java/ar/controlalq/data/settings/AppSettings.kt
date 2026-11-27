package ar.controlalq.data.settings

import ar.controlalq.message.MessageSender

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val numberSeparator: NumberSeparator = NumberSeparator.SYSTEM,
    val messageSender: MessageSender = MessageSender.WhatsApp
)
