package com.example.myapplication.ui.settings

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.data.settings.AppLanguage
import com.example.myapplication.data.settings.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
    val language: StateFlow<AppLanguage> = settingsRepository.language

    fun setThemeMode(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
    }

    fun setLanguage(language: AppLanguage) {
        settingsRepository.setLanguage(language)
    }
}
