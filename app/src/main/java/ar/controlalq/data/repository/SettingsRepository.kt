package ar.controlalq.data.repository

import android.content.Context
import androidx.core.content.edit
import ar.controlalq.data.settings.AppLanguage
import ar.controlalq.data.settings.AppSettings
import ar.controlalq.data.settings.NumberSeparator
import ar.controlalq.data.settings.ThemeMode
import ar.controlalq.message.MessageSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>
    val themeMode: StateFlow<ThemeMode>
    val language: StateFlow<AppLanguage>
    val numberSeparator: StateFlow<NumberSeparator>
    val messageSender: StateFlow<MessageSender>
    fun setThemeMode(mode: ThemeMode)
    fun setLanguage(language: AppLanguage)
    fun setNumberSeparator(separator: NumberSeparator)
    fun setMessageSender(messageSender: MessageSender)
}

class DefaultSettingsRepository(context: Context) : SettingsRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        AppSettings(
            themeMode = loadThemeMode(),
            language = loadLanguage(),
            messageSender = loadMessageSender(),
            numberSeparator = loadNumberSeparator(),
        )
    )
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _themeMode = MutableStateFlow(_settings.value.themeMode)
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(_settings.value.language)
    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _numberSeparator = MutableStateFlow(_settings.value.numberSeparator)
    override val numberSeparator: StateFlow<NumberSeparator> = _numberSeparator.asStateFlow()

    private val _messageSender = MutableStateFlow(_settings.value.messageSender)
    override val messageSender: StateFlow<MessageSender> = _messageSender.asStateFlow()

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        _themeMode.value = updated.themeMode
        _language.value = updated.language
        _messageSender.value = updated.messageSender
        _numberSeparator.value = updated.numberSeparator
    }

    private fun loadThemeMode(): ThemeMode {
        val savedName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(savedName ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun loadLanguage(): AppLanguage {
        val savedName = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.name)
        return try {
            AppLanguage.valueOf(savedName ?: AppLanguage.ENGLISH.name)
        } catch (_: Exception) {
            AppLanguage.ENGLISH
        }
    }

    private fun loadNumberSeparator(): NumberSeparator {
        val savedName = prefs.getString(KEY_NUMBER_SEPARATOR, NumberSeparator.SYSTEM.name)
        return NumberSeparator.entries.find { it.name == savedName } ?: NumberSeparator.SYSTEM
    }

    override fun setNumberSeparator(separator: NumberSeparator) {
        prefs.edit { putString(KEY_NUMBER_SEPARATOR, separator.name) }
        updateSettings { it.copy(numberSeparator = separator) }
    }

    private fun loadMessageSender(): MessageSender {
        val savedName = prefs.getString(KEY_MESSAGE_SENDER, MessageSender.WhatsApp.name)
        return try {
            MessageSender.valueOf(savedName ?: MessageSender.WhatsApp.name)
        } catch (_: Exception) {
            MessageSender.WhatsApp
        }
    }

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        updateSettings { it.copy(themeMode = mode) }
    }

    override fun setLanguage(language: AppLanguage) {
        prefs.edit { putString(KEY_LANGUAGE, language.name) }
        updateSettings { it.copy(language = language) }
    }

    override fun setMessageSender(messageSender: MessageSender) {
        prefs.edit { putString(KEY_MESSAGE_SENDER, messageSender.name) }
        updateSettings { it.copy(messageSender = messageSender) }
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NUMBER_SEPARATOR = "number_separator"
        private const val KEY_MESSAGE_SENDER = "message_sender"
    }
}
