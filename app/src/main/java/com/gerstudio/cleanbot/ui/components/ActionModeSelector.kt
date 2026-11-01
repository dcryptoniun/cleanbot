package com.gerstudio.cleanbot.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.model.PackageActionMode

@Composable
fun ActionModeSelector(
    selectedMode: PackageActionMode,
    keepData: Boolean,
    onModeSelected: (PackageActionMode) -> Unit,
    onKeepDataChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Debloat Action Mode",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedMode == PackageActionMode.DISABLE,
                onClick = { onModeSelected(PackageActionMode.DISABLE) },
                label = { Text("Freeze (Disable)", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = selectedMode == PackageActionMode.UNINSTALL_USER_0 || selectedMode == PackageActionMode.UNINSTALL_USER_0_KEEP_DATA,
                onClick = {
                    onModeSelected(
                        if (keepData) PackageActionMode.UNINSTALL_USER_0_KEEP_DATA
                        else PackageActionMode.UNINSTALL_USER_0
                    )
                },
                label = { Text("Uninstall (User 0)", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.weight(1f)
            )
        }

        if (selectedMode != PackageActionMode.DISABLE) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Checkbox(
                    checked = keepData,
                    onCheckedChange = {
                        onKeepDataChanged(it)
                        onModeSelected(
                            if (it) PackageActionMode.UNINSTALL_USER_0_KEEP_DATA
                            else PackageActionMode.UNINSTALL_USER_0
                        )
                    }
                )
                Text(
                    text = "Keep app data & logins intact (-k flag)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
