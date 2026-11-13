// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.engine

import android.content.Context
import com.gerstudio.cleanbot.adb.AdbSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import java.io.File

class ApkBackupManager(
    private val context: Context,
    private val session: AdbSession,
) {
    suspend fun backupPackage(packageName: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val paths = getApkPaths(packageName)
            if (paths.isEmpty()) {
                error("No APK paths found for package: $packageName")
            }

            // Use app external Downloads dir (scoped-storage safe on Android 11-17)
            val baseDir = context.getExternalFilesDir("backups") ?: File(context.filesDir, "backups")
            val backupDir = File(baseDir, packageName).apply {
                if (!exists()) mkdirs()
            }

            for (remotePath in paths) {
                val fileName = remotePath.substringAfterLast('/')
                val destFile = File(backupDir, fileName)
                destFile.sink().buffer().use { sink ->
                    session.pull(sink, remotePath)
                }
            }

            backupDir
        }
    }

    private suspend fun getApkPaths(packageName: String): List<String> {
        val out = session.shell("pm path $packageName").output
        return out.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("package:") }
            .map { it.substringAfter("package:").trim() }
            .filter { it.endsWith(".apk") }
            .toList()
    }
}
