// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.monitor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.Proc
import com.gerstudio.cleanbot.data.Snapshot
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.SandCard
import com.gerstudio.cleanbot.ui.theme.SignalProgressBar
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

@Composable
fun MonitorScreen(
    snapshot: Snapshot?,
    processes: List<Proc>,
    onKillProcess: (Proc) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()
    val gridState = rememberLazyGridState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanBotPalette.DesertSand)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "TV Performance & Tasks",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Live CPU, memory, storage & running packages",
            style = MaterialTheme.typography.bodySmall,
            color = CleanBotPalette.SandMutedInk,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GaugeCard(
                label = "CPU",
                valueText = if (snapshot != null) "${snapshot.cpuPercent}%" else "—",
                progress = ((snapshot?.cpuPercent ?: 0) / 100f).coerceIn(0f, 1f),
                icon = Icons.Default.Speed,
                modifier = Modifier.weight(1f),
            )
            val ramFrac = if (snapshot != null && snapshot.ramTotalMb > 0) {
                (snapshot.ramUsedMb.toFloat() / snapshot.ramTotalMb).coerceIn(0f, 1f)
            } else 0f
            GaugeCard(
                label = "RAM",
                valueText = if (snapshot != null) "${snapshot.ramUsedMb}/${snapshot.ramTotalMb}MB" else "—",
                progress = ramFrac,
                icon = Icons.Default.Memory,
                modifier = Modifier.weight(1f),
            )
            val usedGb = snapshot?.storageUsedGb ?: 0.0
            val totalGb = snapshot?.storageTotalGb ?: 0.0
            val storageFrac = if (totalGb > 0) (usedGb / totalGb).toFloat().coerceIn(0f, 1f) else 0f
            GaugeCard(
                label = "Storage",
                valueText = if (totalGb > 0) "${"%.1f".format(usedGb)}/${"%.0f".format(totalGb)}G" else "—",
                progress = storageFrac,
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Active Processes (${processes.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CleanBotPalette.OliveDrab,
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 260.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 84.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(processes, key = { it.pid }) { proc ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CleanBotPalette.DesertSandBright,
                    border = BorderStroke(1.dp, CleanBotPalette.SandOutline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = proc.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "PID ${proc.pid} · ${proc.packageName ?: "android system"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CleanBotPalette.SandMutedInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${proc.ramMb} MB",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = CleanBotPalette.OliveDrab,
                            )
                            Text(
                                text = "${"%.1f".format(proc.cpuPercent)}% CPU",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleanBotPalette.SandMutedInk,
                            )
                        }

                        IconButton(
                            onClick = {
                                haptics.reject()
                                onKillProcess(proc)
                            },
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Stop Process",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GaugeCard(
    label: String,
    valueText: String,
    progress: Float,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    SandCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = CleanBotPalette.OliveDrab)
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            SignalProgressBar(progress = progress)
        }
    }
}
