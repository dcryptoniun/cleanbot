package com.gerstudio.cleanbot.ui.screens.files

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.repository.RemoteFsEntry

@Composable
fun TvFileExplorerScreen(
    currentPath: String,
    entries: List<RemoteFsEntry>,
    onNavigateTo: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onInstallApk: (String) -> Unit,
    onUploadFile: () -> Unit,
    onDownloadFile: (RemoteFsEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var apkToInstall by remember { mutableStateOf<RemoteFsEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<RemoteFsEntry?>(null) }

    val pathSegments = remember(currentPath) {
        val clean = currentPath.trim('/').split('/').filter { it.isNotEmpty() }
        listOf("sdcard") + clean.filter { it != "sdcard" }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Breadcrumbs bar
            Surface(
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pathSegments.forEachIndexed { index, segment ->
                        val targetPath = "/" + pathSegments.take(index + 1).joinToString("/")
                        Text(
                            text = segment,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (index == pathSegments.lastIndex) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == pathSegments.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onNavigateTo(targetPath) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        if (index < pathSegments.lastIndex) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Top action buttons row: New Folder & Upload
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${entries.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showNewFolderDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Folder")
                    }
                    TextButton(onClick = onUploadFile) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload")
                    }
                }
            }

            // File items list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(entries) { entry ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (entry.isDirectory) {
                                    onNavigateTo(entry.path)
                                } else if (entry.name.endsWith(".apk", ignoreCase = true)) {
                                    apkToInstall = entry
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when {
                                entry.isDirectory -> Icons.Default.Folder
                                entry.name.endsWith(".apk", ignoreCase = true) -> Icons.Default.Android
                                entry.name.endsWith(".mp4", ignoreCase = true) ||
                                entry.name.endsWith(".mkv", ignoreCase = true) -> Icons.Default.VideoFile
                                else -> Icons.AutoMirrored.Filled.InsertDriveFile
                            }
                            val iconTint = when {
                                entry.isDirectory -> MaterialTheme.colorScheme.primary
                                entry.name.endsWith(".apk", ignoreCase = true) -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Icon(
                                icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (!entry.isDirectory) {
                                    Text(
                                        text = formatFileSize(entry.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!entry.isDirectory) {
                                IconButton(onClick = { onDownloadFile(entry) }) {
                                    Icon(Icons.Default.Download, contentDescription = "Download to Phone", modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(onClick = { entryToDelete = entry }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Create New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateFolder(newFolderName.trim())
                            newFolderName = ""
                            showNewFolderDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Install APK Dialog
    apkToInstall?.let { entry ->
        AlertDialog(
            onDismissRequest = { apkToInstall = null },
            title = { Text("Install APK on TV?") },
            text = { Text("Do you want to install ${entry.name} on the connected TV via ADB?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onInstallApk(entry.path)
                        apkToInstall = null
                    }
                ) {
                    Text("Install")
                }
            },
            dismissButton = {
                TextButton(onClick = { apkToInstall = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete ${if (entry.isDirectory) "Folder" else "File"}?") },
            text = { Text("Are you sure you want to delete ${entry.name} from the TV?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEntry(entry.path)
                        entryToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${"%.1f".format(kb)} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return "${"%.1f".format(mb)} MB"
    val gb = mb / 1024.0
    return "${"%.2f".format(gb)} GB"
}
