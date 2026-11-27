package ar.controlalq.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ar.controlalq.R

enum class RootTab { Active, Inactive, Properties }

@Composable
fun RootNavigationBar(selected: RootTab, onSelected: (RootTab) -> Unit) {
    NavigationBar {
        RootTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelected(tab) },
                icon = {
                    if (tab == RootTab.Inactive) Icon(Icons.Default.Archive, contentDescription = null)
                    else Icon(
                        painterResource(if (tab == RootTab.Properties) R.drawable.baseline_house_24 else R.drawable.ic_contract),
                        contentDescription = null, modifier = Modifier.size(24.dp),
                    )
                },
                label = { Text(stringResource(when (tab) {
                    RootTab.Active -> R.string.tab_active
                    RootTab.Inactive -> R.string.tab_inactive
                    RootTab.Properties -> R.string.title_properties
                })) },
            )
        }
    }
}
