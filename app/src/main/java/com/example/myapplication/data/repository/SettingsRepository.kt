package com.example.myapplication.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.myapplication.data.settings.AppLanguage
import com.example.myapplication.data.settings.AppSettings
import com.example.myapplication.data.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SettingsRepository {
    val themeMode: StateFlow<ThemeMode>
    val language: StateFlow<AppLanguage>
    val settings: StateFlow<AppSettings>
    fun setThemeMode(mode: ThemeMode)
    fun setLanguage(language: AppLanguage)
}

class DefaultSettingsRepository(context: Context) : SettingsRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(loadLanguage())
    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _settings = MutableStateFlow(
        AppSettings(
            themeMode = _themeMode.value,
            language = _language.value,
        ),
    )
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

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

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _themeMode.value = mode
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    override fun setLanguage(language: AppLanguage) {
        prefs.edit { putString(KEY_LANGUAGE, language.name) }
        _language.value = language
        _settings.value = _settings.value.copy(language = language)
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language"
    }
}
