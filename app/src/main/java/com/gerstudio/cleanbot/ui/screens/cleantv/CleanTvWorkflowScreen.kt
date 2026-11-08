// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.cleantv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.Device
import com.gerstudio.cleanbot.data.model.CuratedDebloatPreset
import com.gerstudio.cleanbot.data.model.DebloatRestorePoint
import com.gerstudio.cleanbot.data.model.SafetyClassifier
import com.gerstudio.cleanbot.data.model.TvLauncherInfo
import com.gerstudio.cleanbot.data.model.TvTheme
import com.gerstudio.cleanbot.domain.engine.RecognizedTvProfile
import com.gerstudio.cleanbot.domain.engine.TvBrandRecognizer
import com.gerstudio.cleanbot.ui.components.BatchDebloatSheet
import com.gerstudio.cleanbot.ui.screens.themes.TvMockupPreview

enum class CleanTvStep(val stepNumber: Int, val title: String, val icon: ImageVector) {
    CONNECT(1, "Connect TV", Icons.Default.Tv),
    DEBLOAT(2, "Safe Debloat", Icons.Default.CleaningServices),
    LAUNCHER(3, "New Launcher", Icons.Default.Home),
    THEME(4, "Custom Theme", Icons.Default.Palette)
}

@Composable
fun CleanTvWorkflowScreen(
    device: Device?,
    installedApps: List<AppInfo>,
    restorePoints: List<DebloatRestorePoint>,
    onRescanOrSwitch: () -> Unit,
    onBatchDebloat: (List<String>) -> Unit,
    onUndoRestorePoint: (DebloatRestorePoint) -> Unit,
    onRestoreAllDebloated: () -> Unit,
    onInstallLauncher: (String) -> Unit,
    onSetDefaultHome: (String) -> Unit,
    onGrantAccessibility: (String) -> Unit,
    onPickLauncherApk: () -> Unit,
    onApplyTheme: (TvTheme) -> Unit,
    onResetTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recognized = remember(device, installedApps) {
        TvBrandRecognizer.recognize(device, installedApps)
    }

    var currentStep by remember { mutableStateOf(CleanTvStep.CONNECT) }
    var showDebloatSheet by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(TvTheme.CURATED_THEMES.first()) }

    val disabledAppsCount = remember(installedApps) {
        installedApps.count { !it.enabled && !SafetyClassifier.isProtectedCore(it.packageName, false) }
    }

    // Matching debloat packages for this recognized TV
    val matchingDebloatPkgs = remember(installedApps, recognized) {
        installedApps.filter { app ->
            !SafetyClassifier.isProtectedCore(app.packageName, app.home) &&
                app.enabled &&
                (recognized.recommendedPreset.matches(app.packageName) ||
                    CuratedDebloatPreset.PRESETS.first().matches(app.packageName))
        }.map { it.packageName }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Classy Header with Recognized TV Profile
        TvHeaderCard(
            profile = recognized,
            onSwitchTv = onRescanOrSwitch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        // 4-Step Stepper Tabs
        @Suppress("DEPRECATION")
        ScrollableTabRow(
            selectedTabIndex = currentStep.ordinal,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            CleanTvStep.entries.forEach { step ->
                val isCompleted = when (step) {
                    CleanTvStep.CONNECT -> device != null
                    CleanTvStep.DEBLOAT -> disabledAppsCount > 0
                    CleanTvStep.LAUNCHER -> recognized.isCleanYourTvInstalled || (recognized.currentLauncherPackage != null && recognized.currentLauncherPackage != "com.google.android.apps.tv.launcherx")
                    CleanTvStep.THEME -> recognized.isCleanYourTvActive
                }

                Tab(
                    selected = currentStep == step,
                    onClick = { currentStep = step },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCompleted && currentStep != step) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "${step.stepNumber}. ${step.title}",
                                fontWeight = if (currentStep == step) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Step Content Area
        Box(modifier = Modifier.weight(1f)) {
            when (currentStep) {
                CleanTvStep.CONNECT -> Step1ConnectView(
                    device = device,
                    profile = recognized,
                    installedApps = installedApps,
                    onNext = { currentStep = CleanTvStep.DEBLOAT },
                    onRescan = onRescanOrSwitch
                )
                CleanTvStep.DEBLOAT -> Step2DebloatView(
                    profile = recognized,
                    matchingPackages = matchingDebloatPkgs,
                    disabledCount = disabledAppsCount,
                    restorePoints = restorePoints,
                    onStartDebloat = { showDebloatSheet = true },
                    onUndoRestorePoint = onUndoRestorePoint,
                    onRestoreAll = onRestoreAllDebloated,
                    onNext = { currentStep = CleanTvStep.LAUNCHER }
                )
                CleanTvStep.LAUNCHER -> Step3LauncherView(
                    profile = recognized,
                    installedApps = installedApps,
                    onInstallLauncher = onInstallLauncher,
                    onSetDefaultHome = onSetDefaultHome,
                    onGrantAccessibility = onGrantAccessibility,
                    onPickApk = onPickLauncherApk,
                    onNext = { currentStep = CleanTvStep.THEME }
                )
                CleanTvStep.THEME -> Step4ThemeView(
                    profile = recognized,
                    selectedTheme = selectedTheme,
                    onThemeSelected = { selectedTheme = it },
                    onApplyTheme = { onApplyTheme(selectedTheme) },
                    onResetTheme = onResetTheme
                )
            }
        }
    }

    if (showDebloatSheet && matchingDebloatPkgs.isNotEmpty()) {
        BatchDebloatSheet(
            packages = matchingDebloatPkgs,
            onConfirm = { _, _, _ ->
                onBatchDebloat(matchingDebloatPkgs)
            },
            onDismiss = { showDebloatSheet = false }
        )
    }
}

@Composable
private fun TvHeaderCard(
    profile: RecognizedTvProfile,
    onSwitchTv: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    profile.brand.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.modelName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = profile.brand.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${profile.osVersion} • Current: ${profile.currentLauncherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSwitchTv) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Switch TV",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Step1ConnectView(
    device: Device?,
    profile: RecognizedTvProfile,
    installedApps: List<AppInfo>,
    onNext: () -> Unit,
    onRescan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Wireless ADB Connection Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                val addressText = if (device != null) "${device.hit.ip}:${device.hit.port}" else "Living Room TV"
                Text(
                    text = "Connected over local Wi-Fi to $addressText. No computer, cables, or root privileges needed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Hardware & Recognition Profile
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TV Recognition Details",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                InfoRow("Brand & Ecosystem", profile.brand.displayName)
                InfoRow("Hardware Model", profile.modelName)
                InfoRow("Operating System", profile.osVersion)
                InfoRow("Detected Packages", "${installedApps.size} total apps installed")
                InfoRow("Stock Launcher", profile.currentLauncherName)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Proceed to Step 2: Safe Debloat")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun Step2DebloatView(
    profile: RecognizedTvProfile,
    matchingPackages: List<String>,
    disabledCount: Int,
    restorePoints: List<DebloatRestorePoint>,
    onStartDebloat: () -> Unit,
    onUndoRestorePoint: (DebloatRestorePoint) -> Unit,
    onRestoreAll: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Safety Guarantee
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Safety-First: Reversible Disabling",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "cLeanBot never destructively uninstalls system files. Packages are safely frozen with pm disable-user --user 0. Nothing is deleted and changes are 100% reversible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // 1-Tap Undo Restore Point Card
        if (disabledCount > 0) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TV Restore Point Available",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "$disabledCount packages currently disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                if (restorePoints.isNotEmpty()) {
                                    onUndoRestorePoint(restorePoints.first())
                                } else {
                                    onRestoreAll()
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("1-Tap Undo")
                        }
                    }
                }
            }
        }

        // CleanYourTV Curated Debloat Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        profile.recommendedPreset.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${profile.recommendedPreset.title} (${profile.brand.displayName})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.recommendedPreset.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = profile.recommendedPreset.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${matchingPackages.size} target packages found on TV",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = onStartDebloat,
                        enabled = matchingPackages.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Debloat TV (${matchingPackages.size})")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Proceed to Step 3: Install Custom Launcher")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun Step3LauncherView(
    profile: RecognizedTvProfile,
    installedApps: List<AppInfo>,
    onInstallLauncher: (String) -> Unit,
    onSetDefaultHome: (String) -> Unit,
    onGrantAccessibility: (String) -> Unit,
    onPickApk: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Why Install a Custom Launcher?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Stock Google TV and Fire TV launchers consume up to 450 MB RAM continuously loading ad carousels and video previews. Replacing them with an ultra-lightweight launcher gives your TV an instantaneous, ad-free Apple TV experience.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "Curated Directly-Installable Launchers",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        TvLauncherInfo.CURATED_LAUNCHERS.forEach { launcher ->
            val isInstalled = installedApps.any { it.packageName == launcher.packageName }
            val isActive = installedApps.firstOrNull { it.packageName == launcher.packageName }?.home == true

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = launcher.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (launcher.isCleanYourTvOfficial) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Official CleanYourTV",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = launcher.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else if (isInstalled) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isActive) "Active Home" else if (isInstalled) "Installed" else "Not Installed",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = launcher.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isInstalled) {
                            Button(
                                onClick = { onInstallLauncher(launcher.packageName) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Install on TV")
                            }
                        } else {
                            if (!isActive) {
                                Button(
                                    onClick = { onSetDefaultHome(launcher.packageName) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Make Default Home")
                                }
                            }
                            if (launcher.supportsAccessibilityRemap && launcher.accessibilityServiceComponent != null) {
                                FilledTonalButton(
                                    onClick = { onGrantAccessibility(launcher.accessibilityServiceComponent) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Remap Home Key")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sideload APK Option
        OutlinedButton(
            onClick = onPickApk,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sideload Launcher APK from Phone Storage")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Proceed to Step 4: Choose Custom Theme")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun Step4ThemeView(
    profile: RecognizedTvProfile,
    selectedTheme: TvTheme,
    onThemeSelected: (TvTheme) -> Unit,
    onApplyTheme: () -> Unit,
    onResetTheme: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TV Mockup Preview
        TvMockupPreview(
            theme = selectedTheme,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        // Theme Grid Selector
        Text(
            text = "Curated CleanYourTV Visual Themes",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        val activePrimaryColor = MaterialTheme.colorScheme.primary

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TvTheme.CURATED_THEMES.forEach { theme ->
                val isSelected = selectedTheme.id == theme.id
                val borderMod = if (isSelected) {
                    Modifier.border(2.dp, activePrimaryColor, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { onThemeSelected(theme) }
                        .then(borderMod)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primaryAccent)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = theme.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = theme.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons
        Button(
            onClick = onApplyTheme,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply Theme '${selectedTheme.name}' to TV")
        }

        OutlinedButton(
            onClick = onResetTheme,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset TV to Default Theme")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
