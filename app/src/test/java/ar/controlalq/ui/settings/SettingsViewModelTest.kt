package ar.controlalq.ui.settings

import ar.controlalq.data.repository.SettingsRepository
import ar.controlalq.data.settings.AppLanguage
import ar.controlalq.data.settings.AppSettings
import ar.controlalq.data.settings.NumberSeparator
import ar.controlalq.data.settings.ThemeMode
import ar.controlalq.message.MessageSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsViewModelTest {

    private class FakeSettingsRepository(
        initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
        initialLanguage: AppLanguage = AppLanguage.ENGLISH,
        initialMessageSender: MessageSender = MessageSender.WhatsApp,
    ) : SettingsRepository {
        private val _settings = MutableStateFlow(
            AppSettings(
                themeMode = initialThemeMode,
                language = initialLanguage,
                messageSender = initialMessageSender,
            ),
        )
        override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

        private val _themeMode = MutableStateFlow(initialThemeMode)
        override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

        private val _language = MutableStateFlow(initialLanguage)
        override val language: StateFlow<AppLanguage> = _language.asStateFlow()

        private val _numberSeparator = MutableStateFlow(NumberSeparator.SYSTEM)
        override val numberSeparator = _numberSeparator.asStateFlow()
        override fun setNumberSeparator(separator: NumberSeparator) {
            _numberSeparator.value = separator
            _settings.value = _settings.value.copy(numberSeparator = separator)
        }

        private val _messageSender = MutableStateFlow(initialMessageSender)
        override val messageSender: StateFlow<MessageSender> = _messageSender.asStateFlow()

        override fun setThemeMode(mode: ThemeMode) {
            _themeMode.value = mode
            _settings.value = _settings.value.copy(themeMode = mode)
        }

        override fun setLanguage(language: AppLanguage) {
            _language.value = language
            _settings.value = _settings.value.copy(language = language)
        }

        override fun setMessageSender(messageSender: MessageSender) {
            _messageSender.value = messageSender
            _settings.value = _settings.value.copy(messageSender = messageSender)
        }
    }

    @Test
    fun numberSeparatorUpdatesBothSettingsFlows() {
        val viewModel = SettingsViewModel(FakeSettingsRepository())
        assertEquals(NumberSeparator.SYSTEM, viewModel.numberSeparator.value)
        viewModel.setNumberSeparator(NumberSeparator.COMMA)
        assertEquals(NumberSeparator.COMMA, viewModel.numberSeparator.value)
        assertEquals(NumberSeparator.COMMA, viewModel.settings.value.numberSeparator)
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
