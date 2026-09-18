package com.example.myapplication

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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.repository.BillRepository
import com.example.myapplication.data.repository.ContractRepository
import com.example.myapplication.data.repository.OwnerRepository
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.data.repository.ServiceRepository
import com.example.myapplication.navigation.AppNavigation
import com.example.myapplication.ui.AppViewModelFactory
import com.example.myapplication.ui.contract.ContractViewModel
import com.example.myapplication.ui.contract.create.CreateContractViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val application = applicationContext as MyApplication

            val settingsRepository = application.settingsRepository
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle()
            val language by settingsRepository.language.collectAsStateWithLifecycle()

            LaunchedEffect(language) {
                Locale.setDefault(language.locale)
            }

            val currentContext = LocalContext.current
            val currentConfiguration = LocalConfiguration.current

            val localizedConfiguration = remember(language, currentConfiguration) {
                Configuration(currentConfiguration).apply {
                    setLocale(language.locale)
                }
            }

            val localizedContext = remember(language, currentContext, localizedConfiguration) {
                currentContext.createConfigurationContext(localizedConfiguration)
            }

            val activityResultOwner = currentContext as? ActivityResultRegistryOwner

            val factory = AppViewModelFactory(
                propertyRepository = PropertyRepository(application.database.propertyDao()),
                ownerRepository = OwnerRepository(application.database.ownerDao()),
                serviceRepository = ServiceRepository(application.database.serviceDao()),
                contractRepository = ContractRepository(application.database.contractDao(), application.database.billDao()),
                billStore = BillRepository(application.database.billDao()),
                settingsRepository = settingsRepository,
            )

            val createContractViewModel: CreateContractViewModel by viewModels { factory }
            val contractViewModel: ContractViewModel by viewModels { factory }

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
                MyApplicationTheme(themeMode = themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AppNavigation(
                            createContractViewModel = createContractViewModel,
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
