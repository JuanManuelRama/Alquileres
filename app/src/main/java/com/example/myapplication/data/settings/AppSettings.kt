package com.example.myapplication.data.settings

import com.example.myapplication.message.MessageSender

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val messageSender: MessageSender = MessageSender.WhatsApp
)
