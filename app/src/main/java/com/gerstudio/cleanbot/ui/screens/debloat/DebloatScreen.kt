@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class
)

package com.gerstudio.cleanbot.ui.screens.debloat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.Proc
import com.gerstudio.cleanbot.data.model.SafetyClassifier
import com.gerstudio.cleanbot.data.model.SafetyLevel
import com.gerstudio.cleanbot.ui.components.BatchDebloatSheet
import com.gerstudio.cleanbot.ui.components.MacroChips
import com.gerstudio.cleanbot.ui.components.SafetyBadge
import kotlinx.coroutines.launch

enum class DebloatFilter(val label: String) {
    ALL("All"),
    RECOMMENDED("Recommended Debloat"),
    DISABLED("Disabled"),
    USER_APPS("User Apps")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebloatScreen(
    apps: List<AppInfo>,
    processes: List<Proc>,
    onDisable: (String) -> Unit,
    onEnable: (String) -> Unit,
    onUninstallUser0: (String) -> Unit,
    onExtractApk: (String) -> Unit,
    onBatchDebloat: (List<String>) -> Unit,
    onGrantProjectivy: () -> Unit,
    onToggleFps: () -> Unit,
    onSetDpi: () -> Unit,
    onTrimCaches: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(DebloatFilter.RECOMMENDED) }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var showBatchSheet by remember { mutableStateOf(false) }

    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val coroutineScope = rememberCoroutineScope()

    val filteredApps = remember(apps, query, selectedFilter) {
        apps.filter { app ->
            if (app.packageName == "com.gerstudio.cleanbot" || app.packageName.startsWith("com.gerstudio.cleanbot")) return@filter false
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
            if (app.packageName == "com.gerstudio.cleanbot" || app.packageName.startsWith("com.gerstudio.cleanbot")) return@filter false
            val safety = SafetyClassifier.classify(app.packageName, !app.removable && app.enabled)
            safety == SafetyLevel.SAFE_TO_REMOVE && app.enabled
        }.map { it.packageName }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text("cLeanBot Debloater", fontWeight = FontWeight.Bold)
                            Text(
                                "${filteredApps.size} packages (${apps.count { !it.enabled }} debloated)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )

                // Quick ADB Macro Chips
                MacroChips(
                    onGrantProjectivy = onGrantProjectivy,
                    onToggleFps = onToggleFps,
                    onSetDpi = onSetDpi,
                    onTrimCaches = onTrimCaches,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // Search field
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search packages...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DebloatFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (recommendedDebloatList.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showBatchSheet = true },
                    icon = { Icon(Icons.Default.CleaningServices, contentDescription = null) },
                    text = { Text("Debloat TV (${recommendedDebloatList.size})") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { innerPadding ->
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val safety = SafetyClassifier.classify(app.packageName, !app.removable && app.enabled)
                        val isSelected = selectedApp?.packageName == app.packageName

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedApp = app
                                    coroutineScope.launch {
                                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, app.packageName)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (app.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.label.firstOrNull()?.uppercase() ?: "?",
                                        fontWeight = FontWeight.Bold,
                                        color = if (app.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                SafetyBadge(level = safety)
                            }
                        }
                    }
                }
            },
            detailPane = {
                selectedApp?.let { app ->
                    val proc = processes.firstOrNull { it.packageName == app.packageName }
                    PackageInspectorPane(
                        app = app,
                        liveProcess = proc,
                        onClose = {
                            coroutineScope.launch {
                                navigator.navigateBack()
                            }
                        },
                        onDisable = { onDisable(app.packageName) },
                        onEnable = { onEnable(app.packageName) },
                        onUninstallUser0 = { onUninstallUser0(app.packageName) },
                        onExtractApk = { onExtractApk(app.packageName) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        )
    }

    if (showBatchSheet) {
        BatchDebloatSheet(
            packages = recommendedDebloatList,
            onConfirm = { mode, keepData, backupFirst ->
                onBatchDebloat(recommendedDebloatList)
            },
            onDismiss = { showBatchSheet = false }
        )
    }
}
