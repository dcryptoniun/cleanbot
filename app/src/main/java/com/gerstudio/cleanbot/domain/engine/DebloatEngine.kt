package com.gerstudio.cleanbot.domain.engine

import android.content.Context
import com.gerstudio.cleanbot.adb.AdbSession
import com.gerstudio.cleanbot.data.model.PackageActionMode
import com.gerstudio.cleanbot.data.model.SafetyClassifier
import com.gerstudio.cleanbot.data.model.SafetyLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface DebloatActionResult {
    data class Success(val message: String, val undoAction: PackageActionMode) : DebloatActionResult
    data class Refused(val reason: String) : DebloatActionResult
    data class Failed(val error: String) : DebloatActionResult
}

/**
 * Safety-First Debloat Engine:
 * - Strictly Reversible Disabling via `pm disable-user --user 0`.
 * - System packages are NEVER destructively uninstalled (`pm uninstall --user 0`).
 * - Core protected packages are strictly blocked from being modified.
 * - State restoration via `pm enable --user 0`.
 */
class DebloatEngine(
    private val context: Context,
    private val session: AdbSession
) {
    private val backupManager = ApkBackupManager(context, session)

    suspend fun executeDebloat(
        packageName: String,
        mode: PackageActionMode = PackageActionMode.DISABLE,
        keepData: Boolean = false,
        backupFirst: Boolean = true,
        isCurrentLauncher: Boolean = false,
        isUserInstalledApp: Boolean = false
    ): DebloatActionResult = withContext(Dispatchers.IO) {
        if (SafetyClassifier.isProtectedCore(packageName, isCurrentLauncher)) {
            return@withContext DebloatActionResult.Refused("Blocked: Protected core system component cannot be disabled ($packageName)")
        }

        // System packages are NEVER wiped via uninstall --user 0; they are always safely frozen via disable-user.
        val effectiveMode = if (!isUserInstalledApp) {
            PackageActionMode.DISABLE
        } else {
            mode
        }

        if (backupFirst && effectiveMode != PackageActionMode.DISABLE) {
            backupManager.backupPackage(packageName)
        }

        when (effectiveMode) {
            PackageActionMode.DISABLE -> {
                val res = session.shell("pm disable-user --user 0 $packageName 2>&1")
                if ("disabled-user" in res.output || "new state: disabled-user" in res.output || "disabled" in res.output.lowercase()) {
                    DebloatActionResult.Success("Safely disabled $packageName (Reversible)", PackageActionMode.DISABLE)
                } else if ("SecurityException" in res.output || "not allowed" in res.output) {
                    DebloatActionResult.Refused(res.output.trim())
                } else {
                    DebloatActionResult.Failed(res.output.trim())
                }
            }
            PackageActionMode.UNINSTALL_USER_0,
            PackageActionMode.UNINSTALL_USER_0_KEEP_DATA -> {
                if (!isUserInstalledApp) {
                    return@withContext DebloatActionResult.Refused("Safety Policy: Preinstalled system apps cannot be uninstalled. Use Reversible Disabling instead.")
                }
                val keepFlag = if (keepData || mode == PackageActionMode.UNINSTALL_USER_0_KEEP_DATA) "-k " else ""
                val res = session.shell("pm uninstall ${keepFlag}--user 0 $packageName 2>&1")
                if ("Success" in res.output || "success" in res.output.lowercase()) {
                    DebloatActionResult.Success("Uninstalled user app $packageName", mode)
                } else {
                    DebloatActionResult.Failed(res.output.trim())
                }
            }
        }
    }

    suspend fun restorePackage(packageName: String, wasDisabled: Boolean = true): DebloatActionResult = withContext(Dispatchers.IO) {
        if (wasDisabled) {
            val res = session.shell("pm enable --user 0 $packageName 2>&1")
            if ("enabled" in res.output || "new state: enabled" in res.output || "success" in res.output.lowercase()) {
                DebloatActionResult.Success("Restored (enabled) $packageName", PackageActionMode.DISABLE)
            } else {
                DebloatActionResult.Failed(res.output.trim())
            }
        } else {
            // Restore uninstalled package
            val cmdRes = session.shell("cmd package install-existing $packageName 2>&1")
            if ("installed" in cmdRes.output.lowercase() || "package $packageName" in cmdRes.output.lowercase() || "success" in cmdRes.output.lowercase()) {
                return@withContext DebloatActionResult.Success("Reinstalled $packageName for user 0", PackageActionMode.UNINSTALL_USER_0)
            }
            val pmRes = session.shell("pm install-existing $packageName 2>&1")
            if ("installed" in pmRes.output.lowercase() || "package $packageName" in pmRes.output.lowercase() || "success" in pmRes.output.lowercase()) {
                DebloatActionResult.Success("Reinstalled $packageName for user 0", PackageActionMode.UNINSTALL_USER_0)
            } else {
                DebloatActionResult.Failed(cmdRes.output.ifEmpty { pmRes.output }.trim())
            }
        }
    }

    fun estimateStorageSavedMb(packageCount: Int): Int {
        return packageCount * 45
    }
}
