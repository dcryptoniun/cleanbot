// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data.repository

import com.gerstudio.cleanbot.adb.AdbSession
import com.gerstudio.cleanbot.adb.shellQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Sink
import okio.Source

data class RemoteFsEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val modifiedMillis: Long = 0L,
)

class FileRepository(private val session: AdbSession) {

    suspend fun listDirectory(path: String): List<RemoteFsEntry> = withContext(Dispatchers.IO) {
        val normalized = normalizePath(path)
        runCatching {
            val items = session.list(normalized)
            items.filter { !it.name.startsWith(".") }
                .map { item ->
                    val isDir = (item.mode and S_IFDIR) == S_IFDIR
                    RemoteFsEntry(
                        name = item.name,
                        path = "$normalized/${item.name}".replace("//", "/"),
                        isDirectory = isDir,
                        size = item.size,
                        modifiedMillis = item.mtimeSec * 1000L,
                    )
                }
                .sortedWith(compareByDescending<RemoteFsEntry> { it.isDirectory }.thenBy { it.name.lowercase() })
        }.getOrElse {
            val out = session.shell("ls -la ${shellQuote(normalized)} 2>/dev/null").output
            parseLsOutput(normalized, out)
        }
    }

    suspend fun createFolder(parentPath: String, folderName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanName = folderName.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim('_')
            require(cleanName.isNotEmpty()) { "Invalid folder name" }
            val target = "${normalizePath(parentPath)}/$cleanName".replace("//", "/")
            val r = session.shell("mkdir -p ${shellQuote(target)} 2>&1")
            if (r.exitCode != 0 || "Permission denied" in r.output) {
                error(r.output.trim().ifEmpty { "Could not create folder" })
            }
        }
    }

    suspend fun deleteEntry(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val target = normalizePath(path)
            require(target != "/" && target != "/sdcard" && target != "/storage") { "Refusing to delete protected root directory" }
            val r = session.shell("rm -rf ${shellQuote(target)} 2>&1")
            if (r.exitCode != 0 || "Permission denied" in r.output) {
                error(r.output.trim().ifEmpty { "Could not delete entry" })
            }
        }
    }

    suspend fun installApk(remoteApkPath: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val res = session.shell("pm install -r ${shellQuote(remoteApkPath)} 2>&1")
            if ("Success" in res.output || "success" in res.output.lowercase()) {
                "APK Installed Successfully!"
            } else {
                error("Installation failed: ${res.output.trim()}")
            }
        }
    }

    suspend fun pushFile(source: Source, remotePath: String, size: Long? = null) = withContext(Dispatchers.IO) {
        session.push(source, normalizePath(remotePath), 420, System.currentTimeMillis())
    }

    suspend fun pullFile(remotePath: String, sink: Sink) = withContext(Dispatchers.IO) {
        session.pull(sink, normalizePath(remotePath))
    }

    private fun normalizePath(path: String): String =
        path.trim().replace(Regex("/+"), "/").ifEmpty { "/sdcard" }

    private fun parseLsOutput(parentPath: String, output: String): List<RemoteFsEntry> {
        val list = mutableListOf<RemoteFsEntry>()
        for (line in output.lines()) {
            val parts = line.trim().split(Regex("\\s+"))
            if (parts.size >= 8 && !parts[0].startsWith("total")) {
                val isDir = parts[0].startsWith("d")
                val name = parts.drop(7).joinToString(" ")
                if (name != "." && name != ".." && !name.startsWith(".")) {
                    list.add(
                        RemoteFsEntry(
                            name = name,
                            path = "$parentPath/$name".replace("//", "/"),
                            isDirectory = isDir,
                            size = parts[4].toLongOrNull() ?: 0L,
                            modifiedMillis = System.currentTimeMillis(),
                        ),
                    )
                }
            }
        }
        return list.sortedWith(compareByDescending<RemoteFsEntry> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    companion object {
        private const val S_IFDIR = 0x4000
    }
}
