package com.gerstudio.cleanbot.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.datastore.UserPreferences
import com.gerstudio.cleanbot.data.model.PackageActionMode
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

@Composable
fun CleanBotSettingsScreen(
    preferences: UserPreferences,
    tvMacAddress: String?,
    onSetHapticsEnabled: (Boolean) -> Unit,
    onSetLivingRoomAutoDim: (Boolean) -> Unit,
    onSetDefaultActionMode: (PackageActionMode) -> Unit,
    onSetBackupApkBeforeUninstall: (Boolean) -> Unit,
    onSetKeepDataOnUninstall: (Boolean) -> Unit = {},
    onWakeOnLan: (mac: String) -> Unit,
    onExportPreset: () -> Unit,
    onImportPreset: () -> Unit,
    onResetTv: () -> Unit,
    onOpenTvTools: (() -> Unit)? = null,
    onOpenSupport: (() -> Unit)? = null,
    onRateApp: (() -> Unit)? = null,
    onCheckForUpdate: (() -> Unit)? = null,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBotPalette.DesertSand)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBackButton) {
                IconButton(onClick = { haptics.tap(); onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "Settings & Maintenance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Tactical Palette & Haptics
        Text(
            text = "Appearance & Tactile Feedback",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CleanBotPalette.OliveDrab,
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CleanBotPalette.DesertSandBright),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Signature Palette (60-30-10)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleanBotPalette.OliveDrab,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf(
                        CleanBotPalette.DesertSand to "Desert Sand (60%)",
                        CleanBotPalette.OliveDrab to "Olive Drab (30%)",
                        CleanBotPalette.SignalYellow to "Signal Yellow (10%)",
                    ).forEach { (swatch, name) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(swatch),
                            )
                            Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tactile Haptics",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Vibrate subtly on taps, toggles, and debloat actions",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanBotPalette.SandMutedInk,
                        )
                    }
                    Switch(
                        checked = preferences.hapticsEnabled,
                        onCheckedChange = {
                            haptics.toggle(it)
                            onSetHapticsEnabled(it)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Subtle Remote Glare Reduction",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Softens remote highlights in dim living rooms",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanBotPalette.SandMutedInk,
                        )
                    }
                    Switch(
                        checked = preferences.livingRoomAutoDim,
                        onCheckedChange = {
                            haptics.toggle(it)
                            onSetLivingRoomAutoDim(it)
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Debloat Engine Behavior
        Text(
            text = "Debloat Engine Defaults",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Default Package Action",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = preferences.defaultActionMode == PackageActionMode.DISABLE,
                        onClick = { onSetDefaultActionMode(PackageActionMode.DISABLE) },
                        label = { Text("Freeze / Disable") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = preferences.defaultActionMode == PackageActionMode.UNINSTALL_USER_0,
                        onClick = { onSetDefaultActionMode(PackageActionMode.UNINSTALL_USER_0) },
                        label = { Text("Uninstall (--user 0)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Backup APK before Uninstall",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Extracts and saves APK to Phone before removing from TV",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanBotPalette.SandMutedInk,
                        )
                    }
                    Switch(
                        checked = preferences.backupApkBeforeUninstall,
                        onCheckedChange = {
                            haptics.toggle(it)
                            onSetBackupApkBeforeUninstall(it)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keep Data on User 0 Uninstall",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Pass -k flag when removing packages so logins & caches stay recoverable",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanBotPalette.SandMutedInk,
                        )
                    }
                    Switch(
                        checked = preferences.keepDataOnUninstall,
                        onCheckedChange = {
                            haptics.toggle(it)
                            onSetKeepDataOnUninstall(it)
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: TV Profile & Wake-on-LAN
        Text(
            text = "Multi-TV Profiles & Wake-on-LAN",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Wake TV via Magic Packet (WoL)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (tvMacAddress != null) "MAC: $tvMacAddress" else "MAC address not yet detected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { tvMacAddress?.let(onWakeOnLan) },
                        enabled = tvMacAddress != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wake TV")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Living Room TV Toolkit
        Text(
            text = "Living Room TV Toolkit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sleep timer, Audio-only standby, TV resolution & density scaler, ambient screensaver, and cache purging.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onOpenTvTools?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open TV Toolkit & Sleep Timer")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Shareable Debloat Presets & Reset
        Text(
            text = "Presets & System Recovery",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportPreset,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Preset")
                    }

                    OutlinedButton(
                        onClick = onImportPreset,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Preset")
                    }
                }

                Button(
                    onClick = onResetTv,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset TV (Re-enable & Restore Built-in Apps)")
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Credits & Acknowledgments
        Text(
            text = "About & Community Credits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "cLeanBot v1.3.1",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crafted for high performance, privacy, and clean living room TV experiences.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onOpenSupport != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onOpenSupport,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Support cLeanBot & Tip Jar")
                    }
                }
                if (onRateApp != null) {
                    OutlinedButton(
                        onClick = onRateApp,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rate & Review on Google Play")
                    }
                }
                if (onCheckForUpdate != null) {
                    OutlinedButton(
                        onClick = onCheckForUpdate,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check for Google Play Updates")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Special Acknowledgments & Inspirations:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• CleanYourTV (@Gathin23): Launcher v3.4, themes studio, and curated debloat profiles\n" +
                            "• Tellyscope (@man15h): Foundational companion app architecture and remote\n" +
                            "• Kadb & Dadb (@flyfishxu & Mobile-dev): Pure Kotlin ADB wireless protocol\n" +
                            "• spake2-java (@Flyfish233): Android 11+ SPAKE2 pairing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Text(
                    text = "Licensed under GNU GPL v3. Full terms and third-party notices in NOTICE.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
