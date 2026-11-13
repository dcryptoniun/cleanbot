// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gerstudio.cleanbot.domain.engine.SleepTimerState
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.OliveButton
import com.gerstudio.cleanbot.ui.theme.SandCard
import com.gerstudio.cleanbot.ui.theme.SignalButton
import com.gerstudio.cleanbot.ui.theme.StaggeredCardEntrance
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

@Composable
fun LivingRoomTweaksPane(
    sleepTimerState: SleepTimerState = SleepTimerState(),
    fpsEnabled: Boolean = false,
    currentDpi: Int? = null,
    privateDnsHost: String? = null,
    fastAnimationsEnabled: Boolean = false,
    onOpenPowerAndSleep: () -> Unit = {},
    onToggleFps: () -> Unit,
    onSetDpi: (Int?) -> Unit,
    onTrimCaches: () -> Unit,
    onEnableCleanYourTvKeys: () -> Unit,
    onToggleAdGuardDns: (() -> Unit)? = null,
    onToggleFastAnimations: (() -> Unit)? = null,
    onTakeScreenshot: (() -> Unit)? = null,
    onOpenHistory: (() -> Unit)? = null,
    onOpenAccess: (() -> Unit)? = null,
    onResetTv: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()
    val gridState = rememberLazyGridState()
    var showDpiDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBotPalette.DesertSand),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 84.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                StaggeredCardEntrance(0) {
                    TweakTile(
                        title = "Power, Sleep & Display",
                        description = if (sleepTimerState.active) {
                            "Sleep timer active: ${sleepTimerState.remainingSeconds / 60}m remaining"
                        } else {
                            "Sleep timer, Audio-Only screen off, Screensaver, Soft Reboot, Lock ADB"
                        },
                        actionLabel = if (sleepTimerState.active) "Manage Timer" else "Open Power Sheet",
                        icon = Icons.Default.Bedtime,
                        highlight = true,
                        onAction = { haptics.tap(); onOpenPowerAndSleep() },
                    )
                }
            }

            item {
                StaggeredCardEntrance(1) {
                    TweakTile(
                        title = "CleanYourTV Keys",
                        description = "Enable HomeKeyService & InputKeyService so remote buttons launch CleanYourTV.",
                        actionLabel = "Activate Keys",
                        icon = Icons.Default.Key,
                        onAction = { haptics.confirm(); onEnableCleanYourTvKeys() },
                    )
                }
            }

            item {
                StaggeredCardEntrance(2) {
                    TweakTile(
                        title = "Display DPI Scaling",
                        description = currentDpi?.let { "Currently overriding TV density at ${it}dp." }
                            ?: "Tune UI density (213 to 480 DPI) for sharper TV grids.",
                        actionLabel = currentDpi?.let { "${it}dp (Change)" } ?: "Choose DPI",
                        icon = Icons.Default.Tv,
                        onAction = { haptics.tap(); showDpiDialog = true },
                    )
                }
            }

            item {
                StaggeredCardEntrance(3) {
                    TweakTile(
                        title = "FPS & Frame Overlay",
                        description = if (fpsEnabled) "Real-time GPU refresh overlay is currently ON." else "Show real-time HWUI FPS overlay directly on your TV.",
                        actionLabel = if (fpsEnabled) "Turn OFF" else "Turn ON",
                        icon = Icons.Default.Speed,
                        highlight = fpsEnabled,
                        onAction = { haptics.toggle(!fpsEnabled); onToggleFps() },
                    )
                }
            }

            if (onToggleAdGuardDns != null) {
                item {
                    StaggeredCardEntrance(4) {
                        val adGuardOn = privateDnsHost?.contains("adguard", ignoreCase = true) == true
                        TweakTile(
                            title = "Ad-Block Private DNS",
                            description = if (adGuardOn) "Active: $privateDnsHost" else "Switch TV Private DNS to dns.adguard-dns.com to block ad hosts.",
                            actionLabel = if (adGuardOn) "Reset Auto" else "Enable AdGuard",
                            icon = Icons.Default.Dns,
                            highlight = adGuardOn,
                            onAction = { haptics.toggle(!adGuardOn); onToggleAdGuardDns() },
                        )
                    }
                }
            }

            if (onToggleFastAnimations != null) {
                item {
                    StaggeredCardEntrance(5) {
                        TweakTile(
                            title = "0.5x Fast Animations",
                            description = if (fastAnimationsEnabled) "UI animations sped up (0.5x)." else "Halve system transition duration for snappier TV navigation.",
                            actionLabel = if (fastAnimationsEnabled) "Restore 1.0x" else "Enable 0.5x",
                            icon = Icons.Default.Speed,
                            highlight = fastAnimationsEnabled,
                            onAction = { haptics.toggle(!fastAnimationsEnabled); onToggleFastAnimations() },
                        )
                    }
                }
            }

            item {
                StaggeredCardEntrance(6) {
                    TweakTile(
                        title = "Trim App Caches",
                        description = "Free TV cache storage and reclaim background memory.",
                        actionLabel = "Trim Caches",
                        icon = Icons.Default.CleaningServices,
                        onAction = { haptics.confirm(); onTrimCaches() },
                    )
                }
            }

            if (onTakeScreenshot != null) {
                item {
                    StaggeredCardEntrance(7) {
                        TweakTile(
                            title = "TV Screenshot",
                            description = "Capture your TV screen directly and save to Phone.",
                            actionLabel = "Capture PNG",
                            icon = Icons.Default.CameraAlt,
                            onAction = { haptics.confirm(); onTakeScreenshot() },
                        )
                    }
                }
            }

            if (onOpenHistory != null) {
                item {
                    StaggeredCardEntrance(8) {
                        TweakTile(
                            title = "Change History",
                            description = "Inspect every past debloat/tweak change and undo any action.",
                            actionLabel = "View History",
                            icon = Icons.Default.History,
                            onAction = { haptics.tap(); onOpenHistory() },
                        )
                    }
                }
            }

            if (onOpenAccess != null) {
                item {
                    StaggeredCardEntrance(8) {
                        TweakTile(
                            title = "ADB Access Keys",
                            description = "Audit connected hosts and revoke untrusted ADB keys.",
                            actionLabel = "Manage Access",
                            icon = Icons.Default.Security,
                            onAction = { haptics.tap(); onOpenAccess() },
                        )
                    }
                }
            }

            if (onResetTv != null) {
                item {
                    StaggeredCardEntrance(8) {
                        TweakTile(
                            title = "Reset CleanBot Changes",
                            description = "Re-enable disabled packages and restore stock Home launcher.",
                            actionLabel = "Reset TV",
                            icon = Icons.Default.RestartAlt,
                            onAction = { haptics.reject(); onResetTv() },
                        )
                    }
                }
            }
        }
    }

    if (showDpiDialog) {
        val presets = listOf(
            null to "Default (Reset TV scaling)",
            213 to "213 DPI (Compact 720p TV)",
            240 to "240 DPI (More apps per row)",
            280 to "280 DPI (Balanced)",
            320 to "320 DPI (Standard 1080p)",
            480 to "480 DPI (Large UI)",
        )
        AlertDialog(
            onDismissRequest = { showDpiDialog = false },
            title = { Text("Display DPI Scaling", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { (dpi, label) ->
                        val active = currentDpi == dpi
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) CleanBotPalette.SignalYellow else CleanBotPalette.DesertSandHighest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptics.confirm()
                                    showDpiDialog = false
                                    onSetDpi(dpi)
                                },
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) CleanBotPalette.OnSignalYellow else CleanBotPalette.SandInk,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDpiDialog = false }) { Text("Close") }
            },
        )
    }
}

@Composable
private fun TweakTile(
    title: String,
    description: String,
    actionLabel: String,
    icon: ImageVector,
    highlight: Boolean = false,
    onAction: () -> Unit,
) {
    SandCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (highlight) CleanBotPalette.StatusWarn else CleanBotPalette.OliveDrab,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = CleanBotPalette.SandMutedInk,
                minLines = 2,
            )
            if (highlight) {
                SignalButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                    Text(actionLabel)
                }
            } else {
                OliveButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                    Text(actionLabel)
                }
            }
        }
    }
}
