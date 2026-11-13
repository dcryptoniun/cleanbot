// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.Proc
import com.gerstudio.cleanbot.data.Snapshot
import com.gerstudio.cleanbot.data.datastore.UserPreferences
import com.gerstudio.cleanbot.data.model.PackageActionMode
import com.gerstudio.cleanbot.data.repository.RemoteFsEntry
import com.gerstudio.cleanbot.domain.engine.SleepTimerState
import com.gerstudio.cleanbot.ui.screens.files.TvFileExplorerScreen
import com.gerstudio.cleanbot.ui.screens.monitor.MonitorScreen
import com.gerstudio.cleanbot.ui.screens.settings.CleanBotSettingsScreen
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.ExpressiveStepContent
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

enum class ToolsTab(val label: String, val icon: ImageVector) {
    MONITOR("Monitor", Icons.Default.MonitorHeart),
    FILES("Files", Icons.Default.Folder),
    TWEAKS("TV Tools", Icons.Default.Tune),
    SETTINGS("Settings", Icons.Default.Settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    snapshot: Snapshot?,
    processes: List<Proc>,
    onKillProcess: (Proc) -> Unit,
    currentFilePath: String,
    fileEntries: List<RemoteFsEntry>,
    onNavigateToFileDirectory: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDeleteFileEntry: (String) -> Unit,
    onInstallApk: (String) -> Unit,
    onDownloadFile: (RemoteFsEntry) -> Unit,
    onUploadFile: () -> Unit,
    sleepTimerState: SleepTimerState,
    fpsEnabled: Boolean,
    currentDpi: Int?,
    privateDnsHost: String?,
    fastAnimationsEnabled: Boolean,
    onOpenPowerAndSleep: () -> Unit,
    onToggleFps: () -> Unit,
    onSetDpi: (Int?) -> Unit,
    onTrimCaches: () -> Unit,
    onEnableCleanYourTvKeys: () -> Unit,
    onToggleAdGuardDns: () -> Unit,
    onToggleFastAnimations: () -> Unit,
    onTakeScreenshot: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAccess: () -> Unit,
    onResetTv: () -> Unit,
    preferences: UserPreferences,
    tvMacAddress: String?,
    onSetHapticsEnabled: (Boolean) -> Unit,
    onSetLivingRoomAutoDim: (Boolean) -> Unit,
    onSetDefaultActionMode: (PackageActionMode) -> Unit,
    onSetBackupApkBeforeUninstall: (Boolean) -> Unit,
    onSetKeepDataOnUninstall: (Boolean) -> Unit,
    onWakeOnLan: (String) -> Unit,
    onExportPreset: () -> Unit,
    onImportPreset: () -> Unit,
    onOpenSupport: () -> Unit,
    onRateApp: () -> Unit,
    onCheckForUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBotPalette.DesertSand)
            .statusBarsPadding(),
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = CleanBotPalette.DesertSandBright,
            contentColor = CleanBotPalette.OliveDrab,
        ) {
            ToolsTab.entries.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        haptics.tap()
                        selectedTab = index
                    },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    text = {
                        Text(
                            text = tab.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                        )
                    },
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            ExpressiveStepContent(
                targetState = selectedTab,
            ) { tabIdx ->
                when (tabIdx) {
                    0 -> MonitorScreen(
                        snapshot = snapshot,
                        processes = processes,
                        onKillProcess = onKillProcess,
                    )

                    1 -> TvFileExplorerScreen(
                        currentPath = currentFilePath,
                        entries = fileEntries,
                        onNavigateTo = onNavigateToFileDirectory,
                        onCreateFolder = onCreateFolder,
                        onDeleteEntry = onDeleteFileEntry,
                        onInstallApk = onInstallApk,
                        onDownloadFile = onDownloadFile,
                        onUploadFile = onUploadFile,
                    )

                    2 -> LivingRoomTweaksPane(
                        sleepTimerState = sleepTimerState,
                        fpsEnabled = fpsEnabled,
                        currentDpi = currentDpi,
                        privateDnsHost = privateDnsHost,
                        fastAnimationsEnabled = fastAnimationsEnabled,
                        onOpenPowerAndSleep = onOpenPowerAndSleep,
                        onToggleFps = onToggleFps,
                        onSetDpi = onSetDpi,
                        onTrimCaches = onTrimCaches,
                        onEnableCleanYourTvKeys = onEnableCleanYourTvKeys,
                        onToggleAdGuardDns = onToggleAdGuardDns,
                        onToggleFastAnimations = onToggleFastAnimations,
                        onTakeScreenshot = onTakeScreenshot,
                        onOpenHistory = onOpenHistory,
                        onOpenAccess = onOpenAccess,
                        onResetTv = onResetTv,
                    )

                    else -> CleanBotSettingsScreen(
                        preferences = preferences,
                        tvMacAddress = tvMacAddress,
                        onSetHapticsEnabled = onSetHapticsEnabled,
                        onSetLivingRoomAutoDim = onSetLivingRoomAutoDim,
                        onSetDefaultActionMode = onSetDefaultActionMode,
                        onSetBackupApkBeforeUninstall = onSetBackupApkBeforeUninstall,
                        onSetKeepDataOnUninstall = onSetKeepDataOnUninstall,
                        onWakeOnLan = onWakeOnLan,
                        onExportPreset = onExportPreset,
                        onImportPreset = onImportPreset,
                        onResetTv = onResetTv,
                        onOpenTvTools = onOpenPowerAndSleep,
                        onOpenSupport = onOpenSupport,
                        onRateApp = onRateApp,
                        onCheckForUpdate = onCheckForUpdate,
                        onBack = { selectedTab = 0 },
                        showBackButton = false,
                    )
                }
            }
        }
    }
}
