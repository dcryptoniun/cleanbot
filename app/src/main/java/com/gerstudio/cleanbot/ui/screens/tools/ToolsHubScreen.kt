// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.Proc
import com.gerstudio.cleanbot.data.Snapshot
import com.gerstudio.cleanbot.data.repository.RemoteFsEntry
import com.gerstudio.cleanbot.ui.screens.files.TvFileExplorerScreen
import com.gerstudio.cleanbot.ui.screens.monitor.MonitorScreen

enum class ToolsTab(val label: String, val icon: ImageVector) {
    MONITOR("Monitor", Icons.Default.MonitorHeart),
    FILES("Files", Icons.Default.Folder),
    TWEAKS("TV Tools", Icons.Default.Tune)
}

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
    onToggleFps: () -> Unit,
    onSetDpi: () -> Unit,
    onTrimCaches: () -> Unit,
    onGrantProjectivy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        @Suppress("DEPRECATION")
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            ToolsTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    text = { Text(tab.label, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> MonitorScreen(
                    snapshot = snapshot,
                    processes = processes,
                    onKillProcess = onKillProcess
                )
                1 -> TvFileExplorerScreen(
                    currentPath = currentFilePath,
                    entries = fileEntries,
                    onNavigateTo = onNavigateToFileDirectory,
                    onCreateFolder = onCreateFolder,
                    onDeleteEntry = onDeleteFileEntry,
                    onInstallApk = onInstallApk,
                    onDownloadFile = onDownloadFile,
                    onUploadFile = onUploadFile
                )
                2 -> LivingRoomTweaksPane(
                    onToggleFps = onToggleFps,
                    onSetDpi = onSetDpi,
                    onTrimCaches = onTrimCaches,
                    onGrantProjectivy = onGrantProjectivy
                )
            }
        }
    }
}
