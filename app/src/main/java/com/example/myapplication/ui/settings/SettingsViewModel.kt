package com.example.myapplication.ui.settings

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.data.settings.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode

    fun setThemeMode(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode)
    }
}
