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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

@Composable
fun MacroChips(
    onEnableCleanYourTvKeys: () -> Unit,
    onToggleFps: () -> Unit,
    onSetDpi: () -> Unit,
    onTrimCaches: () -> Unit,
    fpsEnabled: Boolean = false,
    currentDpi: Int? = null,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(
            onClick = { haptics.tap(); onEnableCleanYourTvKeys() },
            label = { Text("CleanYourTV Keys", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            leadingIcon = {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                labelColor = CleanBotPalette.OliveDrab,
                leadingIconContentColor = CleanBotPalette.OliveDrab,
            ),
        )

        AssistChip(
            onClick = { haptics.tap(); onTrimCaches() },
            label = { Text("Trim All Caches", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
            leadingIcon = {
                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                labelColor = CleanBotPalette.OliveDrab,
                leadingIconContentColor = CleanBotPalette.OliveDrab,
            ),
        )

        AssistChip(
            onClick = { haptics.toggle(fpsEnabled); onToggleFps() },
            label = {
                Text(
                    if (fpsEnabled) "FPS Overlay: ON" else "FPS Overlay",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (fpsEnabled) CleanBotPalette.SignalYellow else MaterialTheme.colorScheme.surfaceContainer,
                labelColor = if (fpsEnabled) CleanBotPalette.OnSignalYellow else CleanBotPalette.OliveDrab,
                leadingIconContentColor = if (fpsEnabled) CleanBotPalette.OnSignalYellow else CleanBotPalette.OliveDrab,
            ),
        )

        AssistChip(
            onClick = { haptics.tap(); onSetDpi() },
            label = {
                Text(
                    currentDpi?.let { "DPI: ${it}dp" } ?: "Display DPI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (currentDpi != null) CleanBotPalette.OliveDrab else MaterialTheme.colorScheme.surfaceContainer,
                labelColor = if (currentDpi != null) CleanBotPalette.OnOliveDrab else CleanBotPalette.OliveDrab,
                leadingIconContentColor = if (currentDpi != null) CleanBotPalette.OnOliveDrab else CleanBotPalette.OliveDrab,
            ),
        )
    }
}
