package com.gerstudio.cleanbot.data.repository

import com.gerstudio.cleanbot.adb.AdbSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Sink
import okio.Source

data class RemoteFsEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val modifiedMillis: Long = 0L
)

class FileRepository(private val session: AdbSession) {

    suspend fun listDirectory(path: String): List<RemoteFsEntry> = withContext(Dispatchers.IO) {
        runCatching {
            val items = session.list(path)
            items.filter { !it.name.startsWith(".") }
                .map { item ->
                    val isDir = (item.mode and S_IFDIR) == S_IFDIR
                    RemoteFsEntry(
                        name = item.name,
                        path = "$path/${item.name}".replace("//", "/"),
                        isDirectory = isDir,
                        size = item.size,
                        modifiedMillis = item.mtimeSec * 1000L
                    )
                }
                .sortedWith(compareByDescending<RemoteFsEntry> { it.isDirectory }.thenBy { it.name.lowercase() })
        }.getOrElse {
            // Fallback via ls -la shell
            val out = session.shell("ls -la \"$path\"").output
            parseLsOutput(path, out)
        }
    }

    suspend fun createFolder(parentPath: String, folderName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanName = folderName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val target = "$parentPath/$cleanName".replace("//", "/")
            session.shell("mkdir -p \"$target\"")
            Unit
        }
    }

    suspend fun deleteEntry(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            session.shell("rm -rf \"$path\"")
            Unit
        }
    }

    suspend fun installApk(remoteApkPath: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val res = session.shell("pm install -r \"$remoteApkPath\" 2>&1")
            if ("Success" in res.output || "success" in res.output.lowercase()) {
                "APK Installed Successfully!"
            } else {
                error("Installation failed: ${res.output.trim()}")
            }
        }
    }

    suspend fun pushFile(source: Source, remotePath: String, size: Long? = null) = withContext(Dispatchers.IO) {
        session.push(source, remotePath, 420, System.currentTimeMillis())
    }

    suspend fun pullFile(remotePath: String, sink: Sink) = withContext(Dispatchers.IO) {
        session.pull(sink, remotePath)
    }

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
                            modifiedMillis = System.currentTimeMillis()
                        )
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
