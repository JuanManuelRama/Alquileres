package ar.controlalq

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ar.controlalq.data.repository.BillRepository
import ar.controlalq.data.repository.ContractRepository
import ar.controlalq.data.repository.OwnerRepository
import ar.controlalq.data.repository.PropertyRepository
import ar.controlalq.data.repository.ServiceRepository
import ar.controlalq.data.settings.AppLanguage
import ar.controlalq.data.settings.appLocale
import ar.controlalq.navigation.AppNavigation
import ar.controlalq.ui.AppViewModelFactory
import ar.controlalq.ui.contract.ContractViewModel
import ar.controlalq.ui.theme.AppTheme
import ar.controlalq.ui.util.LocalNumberLocale
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val application = applicationContext as ControlAlq

            val settingsRepository = application.settingsRepository
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle()
            val language by settingsRepository.language.collectAsStateWithLifecycle()
            val numberSeparator by settingsRepository.numberSeparator.collectAsStateWithLifecycle()
            val deviceLocale = LocalConfiguration.current.locales[0]

            val factory = remember(application) {
                AppViewModelFactory(
                    propertyRepository = PropertyRepository(application.database),
                    ownerRepository = OwnerRepository(application.database.ownerDao()),
                    serviceRepository = ServiceRepository(application.database.serviceDao()),
                    contractRepository = ContractRepository(application.database),
                    billStore = BillRepository(application.database.billDao()),
                    settingsRepository = settingsRepository,
                )
            }

            val contractViewModel: ContractViewModel by viewModels { factory }

            CompositionLocalProvider(LocalNumberLocale provides numberSeparator.locale(deviceLocale)) {
            LocalizedContent(language = language) {
                AppTheme(themeMode = themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AppNavigation(
                            contractViewModel = contractViewModel,
                            viewModelFactory = factory,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
            }
        }
    }

    /**
     * Wraps the provided [content] with localized [LocalContext] and [LocalConfiguration] values,
     * ensuring dynamic runtime language switching while preserving device region settings.
     *
     * This helper function:
     * 1. Resolves an app-specific [Locale] combining the target [AppLanguage] with device region preferences.
     * 2. Sets [Locale.setDefault] when the locale changes.
     * 3. Creates a localized [Configuration] and wraps the current [android.content.Context] via [android.content.Context.createConfigurationContext].
     * 4. Provides [LocalContext], [LocalConfiguration], and [LocalActivityResultRegistryOwner] to all descendant composables via [CompositionLocalProvider].
     *
     * @param language The active [AppLanguage] selected in app settings.
     * @param content The composable UI tree to be rendered under the localized context.
     */
    @Composable
    private fun LocalizedContent(
        language: AppLanguage,
        content: @Composable () -> Unit,
    ) {
        val currentContext = LocalContext.current
        val currentConfiguration = LocalConfiguration.current

        // Read the device configuration before providing the app-localized configuration.
        val locale = remember(language, currentConfiguration) {
            appLocale(language.locale, currentConfiguration.locales[0])
        }

        LaunchedEffect(locale) {
            Locale.setDefault(locale)
        }

        val localizedConfiguration = remember(locale, currentConfiguration) {
            Configuration(currentConfiguration).apply {
                setLocale(locale)
            }
        }

        val localizedContext = remember(language, currentContext, localizedConfiguration) {
            currentContext.createConfigurationContext(localizedConfiguration)
        }

        val activityResultOwner = currentContext as? ActivityResultRegistryOwner

        val providers = remember(localizedContext, localizedConfiguration, activityResultOwner) {
            buildList {
                add(LocalContext provides localizedContext)
                add(LocalConfiguration provides localizedConfiguration)
                if (activityResultOwner != null) {
                    add(LocalActivityResultRegistryOwner provides activityResultOwner)
                }
            }.toTypedArray()
        }

        CompositionLocalProvider(*providers) {
            content()
        }
    }
}
