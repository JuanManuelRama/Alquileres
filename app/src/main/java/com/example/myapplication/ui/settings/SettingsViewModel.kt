package com.example.myapplication.ui.settings

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.data.settings.NumberSeparator
import com.example.myapplication.data.settings.AppLanguage
import com.example.myapplication.data.settings.AppSettings
import com.example.myapplication.data.settings.ThemeMode
import com.example.myapplication.message.MessageSender
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings
    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
    val language: StateFlow<AppLanguage> = settingsRepository.language
    val numberSeparator: StateFlow<NumberSeparator> = settingsRepository.numberSeparator
    val messageSender: StateFlow<MessageSender> = settingsRepository.messageSender

    fun setThemeMode(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
    }

    fun setLanguage(language: AppLanguage) {
        settingsRepository.setLanguage(language)
    }

    fun setNumberSeparator(separator: NumberSeparator) {
        settingsRepository.setNumberSeparator(separator)
    }

    fun setMessageSender(messageSender: MessageSender) {
        settingsRepository.setMessageSender(messageSender)
    }
}
