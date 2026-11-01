package com.gerstudio.cleanbot.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MacroChips(
    onGrantProjectivy: () -> Unit,
    onToggleFps: () -> Unit,
    onSetDpi: () -> Unit,
    onTrimCaches: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = onGrantProjectivy,
            label = { Text("Grant Projectivy", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )

        AssistChip(
            onClick = onTrimCaches,
            label = { Text("Trim All Caches", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.CleaningServices,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )

        AssistChip(
            onClick = onToggleFps,
            label = { Text("Toggle 120Hz / FPS", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )

        AssistChip(
            onClick = onSetDpi,
            label = { Text("TV DPI Scale", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Tv,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}
