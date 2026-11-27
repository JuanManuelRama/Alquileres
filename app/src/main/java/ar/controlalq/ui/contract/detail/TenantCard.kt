package ar.controlalq.ui.contract.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ar.controlalq.R
import ar.controlalq.model.Tenant
import ar.controlalq.ui.components.form.formatPhoneNumber
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

@Composable
fun TenantCard(
    tenant: Tenant,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    val configuration = LocalConfiguration.current
    val region = remember(configuration) {
        configuration.locales[0].country.ifBlank { "ZZ" }
    }
    val util = remember(context) { PhoneNumberUtil.createInstance(context) }
    val formattedPhone = remember(tenant.phone, region) {
        formatPhoneNumber(tenant.phone, region, util)
    }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.label_tenant), style = MaterialTheme.typography.labelLarge)
            Text(text = tenant.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.tenant_phone_format, formattedPhone),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
