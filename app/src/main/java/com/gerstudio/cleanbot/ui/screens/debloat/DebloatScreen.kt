// SPDX-License-Identifier: GPL-3.0-only

@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
)

package com.gerstudio.cleanbot.ui.screens.debloat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.layout.WindowInsetsRulers
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fitInside
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.Proc
import com.gerstudio.cleanbot.data.model.PackageActionMode
import com.gerstudio.cleanbot.data.model.SafetyClassifier
import com.gerstudio.cleanbot.data.model.SafetyLevel
import com.gerstudio.cleanbot.ui.components.BatchDebloatSheet
import com.gerstudio.cleanbot.ui.components.DebloatPresetCards
import com.gerstudio.cleanbot.ui.components.MacroChips
import com.gerstudio.cleanbot.ui.components.SafetyBadge
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.StaggeredCardEntrance
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics
import kotlinx.serialization.Serializable

enum class DebloatFilter(val label: String) {
    RECOMMENDED("Safe Clean"),
    USER_APPS("User Apps"),
    DISABLED("Disabled"),
    ALL("All"),
}

@Serializable
private data object DebloatListKey : NavKey

@Serializable
private data class DebloatDetailKey(val packageName: String) : NavKey

@Composable
fun DebloatScreen(
    apps: List<AppInfo>,
    processes: List<Proc>,
    onDisable: (String) -> Unit,
    onEnable: (String) -> Unit,
    onUninstallUser0: (String) -> Unit,
    onExtractApk: (String) -> Unit,
    onBatchDebloat: (List<String>, PackageActionMode, Boolean, Boolean) -> Unit,
    onEnableCleanYourTvKeys: () -> Unit,
    onToggleFps: () -> Unit,
    onSetDpi: (Int?) -> Unit,
    onTrimCaches: () -> Unit,
    fpsEnabled: Boolean = false,
    currentDpi: Int? = null,
    onOpenApp: ((String) -> Unit)? = null,
    onStopProcess: ((Proc) -> Unit)? = null,
    onSideloadApk: (() -> Unit)? = null,
    onExportPreset: (() -> Unit)? = null,
    onImportPreset: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilterName by rememberSaveable { mutableStateOf(DebloatFilter.RECOMMENDED.name) }
    val selectedFilter = remember(selectedFilterName) {
        DebloatFilter.entries.firstOrNull { it.name == selectedFilterName } ?: DebloatFilter.RECOMMENDED
    }
    var showBatchSheet by rememberSaveable { mutableStateOf(false) }
    var showDpiDialog by rememberSaveable { mutableStateOf(false) }

    val backStack = rememberNavBackStack(DebloatListKey)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    val isTwoPane = currentWindowAdaptiveInfoV2().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val selectedPkg = (backStack.lastOrNull() as? DebloatDetailKey)?.packageName

    val filteredApps = remember(apps, query, selectedFilter) {
        apps.filter { app ->
            if (app.packageName == "com.gerstudio.cleanbot" || app.packageName.startsWith("com.gerstudio.cleanbot")) {
                return@filter false
            }
            val matchesQuery = query.isBlank() ||
                app.label.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                DebloatFilter.ALL -> true
                DebloatFilter.RECOMMENDED -> {
                    val safety = SafetyClassifier.classify(app.packageName, !app.removable && app.enabled)
                    safety == SafetyLevel.SAFE_TO_REMOVE && app.enabled
                }
                DebloatFilter.DISABLED -> !app.enabled
                DebloatFilter.USER_APPS -> app.removable
            }
            matchesQuery && matchesFilter
        }
    }

    val recommendedDebloatList = remember(apps) {
        apps.filter { app ->
            if (app.packageName == "com.gerstudio.cleanbot" || app.packageName.startsWith("com.gerstudio.cleanbot")) {
                return@filter false
            }
            val safety = SafetyClassifier.classify(app.packageName, !app.removable && app.enabled)
            safety == SafetyLevel.SAFE_TO_REMOVE && app.enabled
        }.map { it.packageName }
    }

    val gridState = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    NavDisplay(
        backStack = backStack,
        modifier = modifier
            .fillMaxSize()
            .background(CleanBotPalette.DesertSand),
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider = entryProvider {
            entry<DebloatListKey>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CleanBotPalette.DesertSand)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = CleanBotPalette.DesertSandBright,
                                border = BorderStroke(1.dp, CleanBotPalette.SandOutline),
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        Icons.Default.CleaningServices,
                                        contentDescription = null,
                                        tint = CleanBotPalette.OliveDrab,
                                        modifier = Modifier.size(36.dp),
                                    )
                                    Text(
                                        text = "Select an app to inspect",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Disable bloatware, restore packages, inspect live RAM, or back up APKs.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CleanBotPalette.SandMutedInk,
                                    )
                                }
                            }
                        }
                    },
                ),
            ) {
                Scaffold(
                    containerColor = CleanBotPalette.DesertSand,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        Column(modifier = Modifier.background(CleanBotPalette.DesertSand)) {
                            MediumTopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = CleanBotPalette.DesertSand,
                                    scrolledContainerColor = CleanBotPalette.DesertSandBright,
                                    titleContentColor = CleanBotPalette.SandInk,
                                ),
                                title = {
                                    Column {
                                        Text("Apps & Debloater", fontWeight = FontWeight.Bold)
                                        Text(
                                            "${filteredApps.size} shown • ${apps.count { !it.enabled }} disabled",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CleanBotPalette.SandMutedInk,
                                        )
                                    }
                                },
                                actions = {
                                    if (onSideloadApk != null) {
                                        IconButton(onClick = { haptics.tap(); onSideloadApk() }) {
                                            Icon(Icons.Default.InstallMobile, contentDescription = "Sideload APK")
                                        }
                                    }
                                    if (onExportPreset != null) {
                                        IconButton(onClick = { haptics.tap(); onExportPreset() }) {
                                            Icon(Icons.Default.FileUpload, contentDescription = "Export Preset")
                                        }
                                    }
                                    if (onImportPreset != null) {
                                        IconButton(onClick = { haptics.tap(); onImportPreset() }) {
                                            Icon(Icons.Default.FileDownload, contentDescription = "Import Preset")
                                        }
                                    }
                                },
                                scrollBehavior = scrollBehavior,
                            )

                            DebloatPresetCards(
                                installedApps = apps,
                                onApplyPreset = { _, pkgs ->
                                    haptics.confirm()
                                    onBatchDebloat(pkgs, PackageActionMode.DISABLE, false, false)
                                },
                                modifier = Modifier.padding(vertical = 2.dp),
                            )

                            MacroChips(
                                onEnableCleanYourTvKeys = onEnableCleanYourTvKeys,
                                onToggleFps = onToggleFps,
                                onSetDpi = { showDpiDialog = true },
                                onTrimCaches = onTrimCaches,
                                fpsEnabled = fpsEnabled,
                                currentDpi = currentDpi,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )

                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                placeholder = { Text("Search apps or packages…") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (query.isNotEmpty()) {
                                        IconButton(onClick = { query = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                DebloatFilter.entries.forEach { filter ->
                                    val isSelected = selectedFilter == filter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptics.tap()
                                            selectedFilterName = filter.name
                                        },
                                        label = {
                                            Text(
                                                filter.label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CleanBotPalette.OliveDrab,
                                            selectedLabelColor = CleanBotPalette.OnOliveDrab,
                                        ),
                                    )
                                }
                            }
                        }
                    },
                    floatingActionButton = {
                        if (recommendedDebloatList.isNotEmpty()) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    haptics.tap()
                                    showBatchSheet = true
                                },
                                icon = { Icon(Icons.Default.CleaningServices, contentDescription = null) },
                                text = { Text("Clean (${recommendedDebloatList.size})", fontWeight = FontWeight.Bold) },
                                containerColor = CleanBotPalette.SignalYellow,
                                contentColor = CleanBotPalette.OnSignalYellow,
                            )
                        }
                    },
                ) { innerPadding ->
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .consumeWindowInsets(innerPadding)
                            .fitInside(WindowInsetsRulers.Ime.current),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = innerPadding.calculateTopPadding() + 6.dp,
                            bottom = innerPadding.calculateBottomPadding() + 92.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(filteredApps, key = { _, app -> app.packageName }) { index, app ->
                            val safety = SafetyClassifier.classify(app.packageName, !app.removable && app.enabled)
                            val isSelected = selectedPkg == app.packageName
                            StaggeredCardEntrance(delayMillis = index.coerceAtMost(8) * 35) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) CleanBotPalette.OliveDrab.copy(alpha = 0.14f) else CleanBotPalette.DesertSandBright,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) CleanBotPalette.OliveDrab else CleanBotPalette.SandOutline,
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            haptics.tap()
                                            backStack.removeAll { it is DebloatDetailKey }
                                            backStack.add(DebloatDetailKey(app.packageName))
                                        },
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (app.enabled) CleanBotPalette.OliveDrab else CleanBotPalette.DesertSandHighest,
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = app.label.firstOrNull()?.uppercase() ?: "?",
                                                fontWeight = FontWeight.Bold,
                                                color = if (app.enabled) CleanBotPalette.OnOliveDrab else CleanBotPalette.SandMutedInk,
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = app.label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = app.packageName,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = CleanBotPalette.SandMutedInk,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                        SafetyBadge(level = safety)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            entry<DebloatDetailKey>(
                metadata = ListDetailSceneStrategy.detailPane(),
            ) { detail ->
                val app = apps.firstOrNull { it.packageName == detail.packageName }
                if (app != null) {
                    val proc = processes.firstOrNull { it.packageName == app.packageName }
                    PackageInspectorPane(
                        app = app,
                        liveProcess = proc,
                        showClose = !isTwoPane,
                        onClose = { backStack.removeLastOrNull() },
                        onDisable = { haptics.tap(); onDisable(app.packageName) },
                        onEnable = { haptics.confirm(); onEnable(app.packageName) },
                        onUninstallUser0 = { haptics.reject(); onUninstallUser0(app.packageName) },
                        onExtractApk = { haptics.tap(); onExtractApk(app.packageName) },
                        onOpenApp = onOpenApp?.let { fn -> { haptics.tap(); fn(app.packageName) } },
                        onStopProcess = proc?.let { p -> onStopProcess?.let { fn -> { haptics.tap(); fn(p) } } },
                    )
                }
            }
        },
    )

    if (showBatchSheet) {
        BatchDebloatSheet(
            packages = recommendedDebloatList,
            onConfirm = { mode, keepData, backupFirst ->
                haptics.confirm()
                showBatchSheet = false
                onBatchDebloat(recommendedDebloatList, mode, keepData, backupFirst)
            },
            onDismiss = { showBatchSheet = false },
        )
    }

    if (showDpiDialog) {
        DpiPickerDialog(
            currentDpi = currentDpi,
            onSelect = { dpi ->
                haptics.confirm()
                showDpiDialog = false
                onSetDpi(dpi)
            },
            onDismiss = { showDpiDialog = false },
        )
    }
}

@Composable
private fun DpiPickerDialog(
    currentDpi: Int?,
    onSelect: (Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    val presets = listOf(
        null to "Default (Reset TV scaling)",
        213 to "213 DPI (720p Compact TV)",
        240 to "240 DPI (More apps per row)",
        280 to "280 DPI (Balanced)",
        320 to "320 DPI (Standard 1080p)",
        480 to "480 DPI (Large UI / 4K Accessibility)",
    )
    AlertDialog(
        onDismissRequest = onDismiss,
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
                            .clickable { onSelect(dpi) },
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
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}
