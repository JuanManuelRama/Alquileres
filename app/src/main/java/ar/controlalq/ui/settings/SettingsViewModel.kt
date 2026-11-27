package ar.controlalq.ui.settings

import androidx.lifecycle.ViewModel
import ar.controlalq.data.repository.SettingsRepository
import ar.controlalq.data.settings.AppLanguage
import ar.controlalq.data.settings.AppSettings
import ar.controlalq.data.settings.NumberSeparator
import ar.controlalq.data.settings.ThemeMode
import ar.controlalq.message.MessageSender
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
