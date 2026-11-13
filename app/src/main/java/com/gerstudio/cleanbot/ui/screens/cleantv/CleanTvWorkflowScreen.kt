// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.cleantv

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.CLEAN_YOUR_TV_VERSION
import com.gerstudio.cleanbot.data.Device
import com.gerstudio.cleanbot.data.DeviceState
import com.gerstudio.cleanbot.data.Suggestions
import com.gerstudio.cleanbot.data.model.CuratedDebloatPreset
import com.gerstudio.cleanbot.data.model.DebloatRestorePoint
import com.gerstudio.cleanbot.data.model.PackageActionMode
import com.gerstudio.cleanbot.data.model.SafetyClassifier
import com.gerstudio.cleanbot.data.model.TvLauncherInfo
import com.gerstudio.cleanbot.data.model.TvTheme
import com.gerstudio.cleanbot.domain.engine.RecognizedTvProfile
import com.gerstudio.cleanbot.domain.engine.TvBrandRecognizer
import com.gerstudio.cleanbot.ui.CleanYourTvInstallState
import com.gerstudio.cleanbot.ui.LauncherFlow
import com.gerstudio.cleanbot.ui.adaptive.FormFactorPreviews
import com.gerstudio.cleanbot.ui.components.BatchDebloatSheet
import com.gerstudio.cleanbot.ui.screens.themes.TvMockupPreview
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.CleanBotTheme
import com.gerstudio.cleanbot.ui.theme.ExpressiveStepContent
import com.gerstudio.cleanbot.ui.theme.OliveButton
import com.gerstudio.cleanbot.ui.theme.OliveHeroCard
import com.gerstudio.cleanbot.ui.theme.SandCard
import com.gerstudio.cleanbot.ui.theme.SandOutlinedButton
import com.gerstudio.cleanbot.ui.theme.SignalButton
import com.gerstudio.cleanbot.ui.theme.SignalProgressBar
import com.gerstudio.cleanbot.ui.theme.StaggeredCardEntrance
import com.gerstudio.cleanbot.ui.theme.TacticalPulseBeacon
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

enum class CleanTvStep(val stepNumber: Int, val title: String, val icon: ImageVector) {
    DEBLOAT(1, "Safe Clean", Icons.Default.CleaningServices),
    LAUNCHER(2, "Launcher", Icons.Default.Home),
    THEME(3, "TV Look", Icons.Default.Palette),
}

@Composable
fun CleanTvWorkflowScreen(
    device: Device?,
    installedApps: List<AppInfo>,
    restorePoints: List<DebloatRestorePoint>,
    suggestions: Suggestions? = null,
    launcherFlow: LauncherFlow? = null,
    onRescanOrSwitch: () -> Unit,
    onBatchDebloat: (List<String>, PackageActionMode, Boolean, Boolean) -> Unit,
    onUndoRestorePoint: (DebloatRestorePoint) -> Unit,
    onRestoreAllDebloated: () -> Unit,
    onInstallCleanYourTv: (Boolean) -> Unit,
    onSetDefaultHome: (String) -> Unit,
    onGrantAccessibility: (String) -> Unit,
    onPickLauncherApk: () -> Unit,
    onApplyTheme: (TvTheme) -> Unit,
    onResetTheme: () -> Unit,
    onOpenModelDebloat: (() -> Unit)? = null,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()
    val recognized = remember(device, installedApps) {
        TvBrandRecognizer.recognize(device, installedApps)
    }

    var currentStepName by rememberSaveable { mutableStateOf(CleanTvStep.DEBLOAT.name) }
    val currentStep = remember(currentStepName) {
        CleanTvStep.entries.firstOrNull { it.name == currentStepName } ?: CleanTvStep.DEBLOAT
    }
    var showDebloatSheet by rememberSaveable { mutableStateOf(false) }
    var selectedThemeId by rememberSaveable { mutableStateOf(TvTheme.CURATED_THEMES.first().id) }
    val selectedTheme = remember(selectedThemeId) {
        TvTheme.CURATED_THEMES.firstOrNull { it.id == selectedThemeId } ?: TvTheme.CURATED_THEMES.first()
    }

    val disabledAppsCount = remember(installedApps) {
        installedApps.count { !it.enabled && !SafetyClassifier.isProtectedCore(it.packageName, false) }
    }

    val modelSuggestionPkgs = remember(suggestions) {
        when (suggestions) {
            is Suggestions.Ready -> suggestions.groups.flatMap { g ->
                g.packages.filter { it.ticked || !it.optional }.map { it.pkg }
            }.distinct()
            else -> emptyList()
        }
    }

    val matchingDebloatPkgs = remember(installedApps, recognized, modelSuggestionPkgs) {
        val enabledMap = installedApps.associateBy { it.packageName }
        val fromModel = modelSuggestionPkgs.filter { pkg ->
            val app = enabledMap[pkg]
            app != null && app.enabled && !SafetyClassifier.isProtectedCore(pkg, app.home)
        }
        if (fromModel.isNotEmpty()) {
            fromModel
        } else {
            installedApps.filter { app ->
                !SafetyClassifier.isProtectedCore(app.packageName, app.home) &&
                    app.enabled &&
                    (recognized.recommendedPreset.matches(app.packageName) ||
                        CuratedDebloatPreset.PRESETS.first().matches(app.packageName))
            }.map { it.packageName }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBotPalette.DesertSand)
            .statusBarsPadding(),
    ) {
        TvHeroBanner(
            device = device,
            profile = recognized,
            matchedListName = (suggestions as? Suggestions.Ready)?.list?.name,
            disabledCount = disabledAppsCount,
            onSwitchTv = { haptics.tap(); onRescanOrSwitch() },
            onOpenSettings = { haptics.tap(); onOpenSettings() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )

        StepPillBar(
            currentStep = currentStep,
            debloatDone = disabledAppsCount > 0,
            launcherDone = recognized.isCleanYourTvInstalled,
            themeDone = recognized.isCleanYourTvActive,
            onSelect = { step ->
                haptics.tap()
                currentStepName = step.name
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(modifier = Modifier.weight(1f)) {
            ExpressiveStepContent(
                targetState = currentStep,
            ) { step ->
                when (step) {
                    CleanTvStep.DEBLOAT -> StepDebloatView(
                        device = device,
                        profile = recognized,
                        matchedList = (suggestions as? Suggestions.Ready),
                        matchingPackages = matchingDebloatPkgs,
                        disabledCount = disabledAppsCount,
                        restorePoints = restorePoints,
                        onStartDebloat = { haptics.tap(); showDebloatSheet = true },
                        onOpenModelDebloat = onOpenModelDebloat,
                        onUndoRestorePoint = { point -> haptics.confirm(); onUndoRestorePoint(point) },
                        onRestoreAll = { haptics.confirm(); onRestoreAllDebloated() },
                        onNext = { haptics.tap(); currentStepName = CleanTvStep.LAUNCHER.name },
                    )

                    CleanTvStep.LAUNCHER -> StepLauncherView(
                        installedApps = installedApps,
                        launcherFlow = launcherFlow,
                        onInstallBundled = { keys -> haptics.confirm(); onInstallCleanYourTv(keys) },
                        onSetDefaultHome = { pkg -> haptics.confirm(); onSetDefaultHome(pkg) },
                        onGrantAccessibility = { svc -> haptics.tap(); onGrantAccessibility(svc) },
                        onPickApk = { haptics.tap(); onPickLauncherApk() },
                        onNext = { haptics.tap(); currentStepName = CleanTvStep.THEME.name },
                    )

                    CleanTvStep.THEME -> StepThemeView(
                        isLauncherInstalled = recognized.isCleanYourTvInstalled,
                        selectedTheme = selectedTheme,
                        onThemeSelected = { theme -> haptics.tap(); selectedThemeId = theme.id },
                        onApplyTheme = { haptics.confirm(); onApplyTheme(selectedTheme) },
                        onResetTheme = { haptics.tap(); onResetTheme() },
                    )
                }
            }
        }
    }

    if (showDebloatSheet && matchingDebloatPkgs.isNotEmpty()) {
        BatchDebloatSheet(
            packages = matchingDebloatPkgs,
            onConfirm = { mode, keepData, backupFirst ->
                haptics.confirm()
                showDebloatSheet = false
                onBatchDebloat(matchingDebloatPkgs, mode, keepData, backupFirst)
            },
            onDismiss = { showDebloatSheet = false },
        )
    }
}

@Composable
private fun TvHeroBanner(
    device: Device?,
    profile: RecognizedTvProfile,
    matchedListName: String?,
    disabledCount: Int,
    onSwitchTv: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isOnline = device?.state == DeviceState.Online
    OliveHeroCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TacticalPulseBeacon(
                active = isOnline,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device?.nickname ?: device?.model ?: profile.modelName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CleanBotPalette.OnOliveDrab,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildString {
                        append(profile.osVersion)
                        append(" • ")
                        append(profile.currentLauncherName)
                        if (disabledCount > 0) append(" • $disabledCount cleaned")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = CleanBotPalette.DesertSand,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (matchedListName != null) {
                    Text(
                        text = "cleanyourtv list: $matchedListName",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = CleanBotPalette.SignalYellow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onSwitchTv) {
                Icon(Icons.Default.Refresh, contentDescription = "Switch TV", tint = CleanBotPalette.OnOliveDrab)
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = CleanBotPalette.OnOliveDrab)
            }
        }
    }
}

@Composable
private fun StepPillBar(
    currentStep: CleanTvStep,
    debloatDone: Boolean,
    launcherDone: Boolean,
    themeDone: Boolean,
    onSelect: (CleanTvStep) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CleanTvStep.entries.forEach { step ->
            val selected = step == currentStep
            val completed = when (step) {
                CleanTvStep.DEBLOAT -> debloatDone
                CleanTvStep.LAUNCHER -> launcherDone
                CleanTvStep.THEME -> themeDone
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when {
                    selected -> CleanBotPalette.SignalYellow
                    completed -> CleanBotPalette.OliveDrab.copy(alpha = 0.14f)
                    else -> CleanBotPalette.DesertSandBright
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) CleanBotPalette.SignalYellow else CleanBotPalette.SandOutline,
                ),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(step) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (completed && !selected) Icons.Default.CheckCircle else step.icon,
                        contentDescription = null,
                        tint = when {
                            selected -> CleanBotPalette.OnSignalYellow
                            completed -> CleanBotPalette.OliveDrab
                            else -> CleanBotPalette.SandMutedInk
                        },
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${step.stepNumber}. ${step.title}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (selected) CleanBotPalette.OnSignalYellow else CleanBotPalette.SandInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepDebloatView(
    device: Device?,
    profile: RecognizedTvProfile,
    matchedList: Suggestions.Ready?,
    matchingPackages: List<String>,
    disabledCount: Int,
    restorePoints: List<DebloatRestorePoint>,
    onStartDebloat: () -> Unit,
    onOpenModelDebloat: (() -> Unit)?,
    onUndoRestorePoint: (DebloatRestorePoint) -> Unit,
    onRestoreAll: () -> Unit,
    onNext: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val estMb = matchingPackages.size * 18

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StaggeredCardEntrance(delayMillis = 0) {
            SandCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = matchedList?.list?.name ?: "${profile.recommendedPreset.title} (${profile.brand.displayName})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CleanBotPalette.OliveDrab,
                        ) {
                            Text(
                                text = if (matchedList != null) "cleanyourtv Match" else "Smart Preset",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CleanBotPalette.OnOliveDrab,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Text(
                        text = if (matchedList != null) {
                            "Matched directly from Gathin23/cleanyourtv catalog (${matchedList.groups.size} groups). Tested on ${matchedList.list.testedOs ?: profile.osVersion}."
                        } else {
                            profile.recommendedPreset.description
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanBotPalette.SandMutedInk,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StatChip("${matchingPackages.size} Safe Targets")
                        StatChip("~${estMb} MB Freeable")
                        if (disabledCount > 0) StatChip("$disabledCount Already Cleaned", highlight = true)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    SignalButton(
                        onClick = onStartDebloat,
                        enabled = device != null && matchingPackages.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (matchingPackages.isNotEmpty()) {
                                "Clean ${matchingPackages.size} Safe Apps Now"
                            } else {
                                "All Matched Bloat Already Cleaned"
                            },
                        )
                    }

                    if (onOpenModelDebloat != null && matchedList != null) {
                        SandOutlinedButton(
                            onClick = onOpenModelDebloat,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Review Full ${matchedList.list.name} List")
                        }
                    }
                }
            }
        }

        val activeRestore = restorePoints.filter { it.active }
        if (activeRestore.isNotEmpty() || disabledCount > 0) {
            StaggeredCardEntrance(delayMillis = 35) {
                SandCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Safety & 1-Tap Undo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CleanBotPalette.OliveDrab,
                        )
                        activeRestore.take(3).forEach { pt ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CleanBotPalette.DesertSandHighest,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pt.presetName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text("${pt.disabledPackages.size} apps • ${pt.tvBrand}", style = MaterialTheme.typography.labelSmall, color = CleanBotPalette.SandMutedInk)
                                    }
                                    TextButton(onClick = { onUndoRestorePoint(pt) }) {
                                        Icon(Icons.AutoMirrored.Filled.Undo, null, Modifier.size(16.dp), tint = CleanBotPalette.OliveDrab)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Undo", color = CleanBotPalette.OliveDrab, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        if (disabledCount > 0) {
                            SandOutlinedButton(onClick = onRestoreAll, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.RestartAlt, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Restore All $disabledCount Disabled Apps")
                            }
                        }
                    }
                }
            }
        }

        OliveButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Next: CleanYourTV Launcher v$CLEAN_YOUR_TV_VERSION")
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun StepLauncherView(
    installedApps: List<AppInfo>,
    launcherFlow: LauncherFlow?,
    onInstallBundled: (Boolean) -> Unit,
    onSetDefaultHome: (String) -> Unit,
    onGrantAccessibility: (String) -> Unit,
    onPickApk: () -> Unit,
    onNext: () -> Unit,
) {
    val launcher = TvLauncherInfo.CLEAN_YOUR_TV
    val existing = installedApps.firstOrNull { it.packageName == launcher.packageName }
    val isInstalled = existing != null
    val isActive = existing?.home == true
    val isInstalling = launcherFlow?.step == LauncherFlow.Step.Installing
    var autoKeys by rememberSaveable { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StaggeredCardEntrance(delayMillis = 0) {
            SandCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = launcher.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Version ${launcher.versionName} • Offline bundled APK",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleanBotPalette.SandMutedInk,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isActive -> CleanBotPalette.SignalYellow
                                isInstalled -> CleanBotPalette.OliveDrab
                                else -> CleanBotPalette.DesertSandHighest
                            },
                        ) {
                            Text(
                                text = when {
                                    isActive -> "Active Home"
                                    isInstalled -> "Installed"
                                    else -> "Ready to Install"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isActive -> CleanBotPalette.OnSignalYellow
                                    isInstalled -> CleanBotPalette.OnOliveDrab
                                    else -> CleanBotPalette.SandInk
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Text(
                        text = launcher.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanBotPalette.SandMutedInk,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { autoKeys = !autoKeys }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = autoKeys,
                            onCheckedChange = { autoKeys = it },
                            colors = CheckboxDefaults.colors(checkedColor = CleanBotPalette.OliveDrab),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Auto-enable Home & Input Key Services",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Ensures your TV remote Home button opens CleanYourTV cleanly",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleanBotPalette.SandMutedInk,
                            )
                        }
                    }

                    AnimatedVisibility(visible = isInstalling) {
                        val frac = launcherFlow?.bundled?.progress?.let { p ->
                            p.total?.takeIf { it > 0L }?.let { (p.done.toFloat() / it.toFloat()).coerceIn(0f, 1f) }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = when (launcherFlow?.bundled?.phase) {
                                    CleanYourTvInstallState.Phase.Preparing -> "Verifying tvhome.apk checksum…"
                                    CleanYourTvInstallState.Phase.Installing -> "Installing CleanYourTV onto TV…"
                                    CleanYourTvInstallState.Phase.Configuring -> "Running ART compile & key setup…"
                                    else -> "Installing…"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CleanBotPalette.OliveDrab,
                            )
                            SignalProgressBar(progress = frac ?: 0.5f)
                        }
                    }

                    val err = launcherFlow?.bundled?.error
                    if (err != null) {
                        Text(
                            text = "Install notice: Check TV connection or sideload manually below.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    if (!isInstalled) {
                        SignalButton(
                            onClick = { onInstallBundled(autoKeys) },
                            enabled = !isInstalling,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isInstalling) "Installing…" else "1-Tap Install CleanYourTV v${launcher.versionName}")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (!isActive) {
                                SignalButton(
                                    onClick = { onSetDefaultHome(launcher.packageName) },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Set as Home")
                                }
                            }
                            launcher.accessibilityServiceComponent.let { svc ->
                                SandOutlinedButton(
                                    onClick = { onGrantAccessibility(svc) },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Fix Remote Keys")
                                }
                            }
                        }
                    }
                }
            }
        }

        SandOutlinedButton(onClick = onPickApk, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Install Custom tvhome.apk from Phone")
        }

        OliveButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Next: Apply Desert & Olive TV Look")
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun StepThemeView(
    isLauncherInstalled: Boolean,
    selectedTheme: TvTheme,
    onThemeSelected: (TvTheme) -> Unit,
    onApplyTheme: () -> Unit,
    onResetTheme: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val themeRowScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StaggeredCardEntrance(delayMillis = 0) {
            TvMockupPreview(
                theme = selectedTheme,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = "CleanYourTV Preset Looks",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = CleanBotPalette.OliveDrab,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(themeRowScroll),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TvTheme.CURATED_THEMES.forEach { theme ->
                val selected = theme.id == selectedTheme.id
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) CleanBotPalette.SignalYellow.copy(alpha = 0.25f) else CleanBotPalette.DesertSandBright,
                    border = BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) CleanBotPalette.OliveDrab else CleanBotPalette.SandOutline,
                    ),
                    modifier = Modifier
                        .width(136.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onThemeSelected(theme) },
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primaryAccent)
                                .border(1.dp, theme.backgroundColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(theme.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(theme.subtitle, style = MaterialTheme.typography.labelSmall, color = CleanBotPalette.SandMutedInk, maxLines = 1)
                    }
                }
            }
        }

        SignalButton(
            onClick = onApplyTheme,
            enabled = isLauncherInstalled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isLauncherInstalled) "Push '${selectedTheme.name}' to TV"
                else "Install CleanYourTV in Step 2 First",
            )
        }

        SandOutlinedButton(
            onClick = onResetTheme,
            enabled = isLauncherInstalled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset CleanYourTV Theme")
        }
    }
}

@Composable
private fun StatChip(label: String, highlight: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (highlight) CleanBotPalette.SignalYellow else CleanBotPalette.DesertSandHighest,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) CleanBotPalette.OnSignalYellow else CleanBotPalette.OliveDrab,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@FormFactorPreviews
@Composable
private fun CleanTvWorkflowScreenPreview() {
    CleanBotTheme {
        CleanTvWorkflowScreen(
            device = null,
            installedApps = emptyList(),
            restorePoints = emptyList(),
            onRescanOrSwitch = {},
            onBatchDebloat = { _, _, _, _ -> },
            onUndoRestorePoint = {},
            onRestoreAllDebloated = {},
            onInstallCleanYourTv = {},
            onSetDefaultHome = {},
            onGrantAccessibility = {},
            onPickLauncherApk = {},
            onApplyTheme = {},
            onResetTheme = {},
        )
    }
}
