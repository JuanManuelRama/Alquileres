package com.example.myapplication.ui.settings

import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.data.settings.AppSettings
import com.example.myapplication.data.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsViewModelTest {

    private class FakeSettingsRepository(
        initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
    ) : SettingsRepository {
        private val _themeMode = MutableStateFlow(initialThemeMode)
        override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

        private val _settings = MutableStateFlow(AppSettings(themeMode = initialThemeMode))
        override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

        override fun setThemeMode(mode: ThemeMode) {
            _themeMode.value = mode
            _settings.value = AppSettings(themeMode = mode)
        }
    }

    @Test
    fun defaultThemeModeIsSystem() {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun updatingThemeModeChangesState() {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.setThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)

        viewModel.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, viewModel.themeMode.value)
    }
}
