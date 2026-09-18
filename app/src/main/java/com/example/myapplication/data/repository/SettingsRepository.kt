package com.example.myapplication.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.myapplication.data.settings.AppSettings
import com.example.myapplication.data.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SettingsRepository {
    val themeMode: StateFlow<ThemeMode>
    val settings: StateFlow<AppSettings>
    fun setThemeMode(mode: ThemeMode)
}

class DefaultSettingsRepository(context: Context) : SettingsRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings(themeMode = _themeMode.value))
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadThemeMode(): ThemeMode {
        val savedName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(savedName ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _themeMode.value = mode
        _settings.value = AppSettings(themeMode = mode)
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
