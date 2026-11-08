// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LivingRoomTweaksPane(
    onToggleFps: () -> Unit,
    onSetDpi: () -> Unit,
    onTrimCaches: () -> Unit,
    onGrantProjectivy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Living Room TV Toolkit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // DPI Density Card
        TweakCard(
            title = "Display DPI Density",
            description = "Adjust TV scaling between 240, 280, 320, and 480 DPI for more screen estate or larger text.",
            actionLabel = "Adjust DPI",
            icon = Icons.Default.Tv,
            onAction = onSetDpi
        )

        // FPS Overlay Card
        TweakCard(
            title = "Real-Time FPS & Refresh Rate",
            description = "Toggle the system HWUI refresh rate overlay directly on your TV display to measure smoothness.",
            actionLabel = "Toggle Overlay",
            icon = Icons.Default.Speed,
            onAction = onToggleFps
        )

        // Trim Caches Card
        TweakCard(
            title = "Trim System Caches",
            description = "Signals Android's ActivityManager to trim runtime memory and caches across all background processes.",
            actionLabel = "Trim Memory",
            icon = Icons.Default.CleaningServices,
            onAction = onTrimCaches
        )

        // Grant Home Remap Card
        TweakCard(
            title = "Grant Home Button Remap",
            description = "Instantly grants secure accessibility permissions over ADB so alternative launchers can intercept the TV remote Home key.",
            actionLabel = "Grant Permission",
            icon = Icons.Default.Key,
            onAction = onGrantProjectivy
        )
    }
}

@Composable
private fun TweakCard(
    title: String,
    description: String,
    actionLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FilledTonalButton(
                onClick = onAction,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(actionLabel)
            }
        }
    }
}
