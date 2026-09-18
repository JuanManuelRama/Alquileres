package com.example.myapplication.ui.settings

import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.data.settings.AppLanguage
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
        initialLanguage: AppLanguage = AppLanguage.ENGLISH,
    ) : SettingsRepository {
        private val _themeMode = MutableStateFlow(initialThemeMode)
        override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

        private val _language = MutableStateFlow(initialLanguage)
        override val language: StateFlow<AppLanguage> = _language.asStateFlow()

        private val _settings = MutableStateFlow(
            AppSettings(
                themeMode = initialThemeMode,
                language = initialLanguage,
            ),
        )
        override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

        override fun setThemeMode(mode: ThemeMode) {
            _themeMode.value = mode
            _settings.value = _settings.value.copy(themeMode = mode)
        }

        override fun setLanguage(language: AppLanguage) {
            _language.value = language
            _settings.value = _settings.value.copy(language = language)
        }
    }

    @Test
    fun defaultThemeModeIsSystem() {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        assertEquals(ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun defaultLanguageIsEnglish() {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)
        assertEquals(AppLanguage.ENGLISH, viewModel.language.value)
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

    @Test
    fun updatingLanguageChangesState() {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.setLanguage(AppLanguage.SPANISH)
        assertEquals(AppLanguage.SPANISH, viewModel.language.value)

        viewModel.setLanguage(AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, viewModel.language.value)
    }
}
