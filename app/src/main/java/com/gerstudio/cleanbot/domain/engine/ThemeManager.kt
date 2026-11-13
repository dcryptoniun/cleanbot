// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.engine

import android.content.Context
import android.util.Base64
import com.gerstudio.cleanbot.adb.AdbSession
import com.gerstudio.cleanbot.adb.HomeServices
import com.gerstudio.cleanbot.adb.shellQuote
import com.gerstudio.cleanbot.data.model.TvLauncherInfo
import com.gerstudio.cleanbot.data.model.TvTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Applies CleanYourTV Launcher (`com.gathin.tvhome`) overlays and manages Home switching
 * following `Gathin23/cleanyourtv/tools/theme.sh` & `ThemeProvider.java`.
 */
class ThemeManager(
    @Suppress("UNUSED_PARAMETER") private val context: Context,
    private val session: AdbSession,
) {
    private val pkg = TvLauncherInfo.CLEAN_YOUR_TV_PKG
    private val filesDir = "/sdcard/Android/data/$pkg/files"
    private val providerUri = "content://$pkg.theme"

    /**
     * Writes `config.json` + `theme_revision` via `ThemeProvider` (`content write`, avoiding
     * Android 11+ / Android 14+ shell UID `EACCES`), with `cp` fallback, then restarts Home
     * while preserving CleanYourTV's enabled key accessibility services.
     */
    suspend fun applyThemeToTv(theme: TvTheme): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonContent = theme.toCleanYourTvJson()
            val revision = UUID.randomUUID().toString().replace("-", "")
            val stageCfg = "/data/local/tmp/cyt-config.json"
            val stageRev = "/data/local/tmp/cyt-theme_revision"

            writeStagedText(stageCfg, jsonContent)
            writeStagedText(stageRev, revision)

            val wroteCfg = pushViaProviderOrCopy(stageCfg, "config.json")
            val wroteRev = pushViaProviderOrCopy(stageRev, "theme_revision")

            session.shell("rm -f $stageCfg $stageRev 2>/dev/null")
            check(wroteCfg && wroteRev) {
                "The TV refused writing theme files to CleanYourTV ($pkg). Ensure CleanYourTV Launcher v3.4 is installed."
            }

            restartHomePreservingKeyServices()
            "Applied '${theme.name}' to CleanYourTV Launcher"
        }
    }

    /**
     * Resets pushed `config.json` & wallpapers back to CleanYourTV's built-in defaults (`theme.sh --reset`).
     */
    suspend fun resetTvThemeToDefault(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val revision = UUID.randomUUID().toString().replace("-", "")
            val stageRev = "/data/local/tmp/cyt-theme_revision"
            writeStagedText(stageRev, revision)

            session.shell(
                "rm -f $filesDir/config.json $filesDir/wallpaper.jpg $filesDir/wallpapers/captions.txt 2>/dev/null; " +
                    "content delete --uri $providerUri/config.json 2>/dev/null; " +
                    "content delete --uri $providerUri/wallpaper.jpg 2>/dev/null; " +
                    "content delete --uri $providerUri/wallpapers 2>/dev/null",
            )
            pushViaProviderOrCopy(stageRev, "theme_revision")
            session.shell("rm -f $stageRev 2>/dev/null")

            restartHomePreservingKeyServices()
            "Reset CleanYourTV theme to built-in defaults"
        }
    }

    suspend fun setDefaultHomeLauncher(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val component = if (packageName == TvLauncherInfo.CLEAN_YOUR_TV_PKG) {
                TvLauncherInfo.CLEAN_YOUR_TV_ACTIVITY
            } else {
                packageName
            }
            val out = session.shell("cmd package set-home-activity --user 0 ${shellQuote(component)} 2>&1").output
            if ("error" in out.lowercase()) {
                session.shell("am start -W -a android.intent.action.MAIN -c android.intent.category.HOME -n ${shellQuote(component)} 2>/dev/null || input keyevent 3")
            } else {
                session.shell("input keyevent 3")
            }
            "Default Home set to $packageName"
        }
    }

    suspend fun grantLauncherAccessibility(serviceComponent: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val targetPkg = serviceComponent.substringBefore('/')
            session.shell("cmd appops set $targetPkg AUTO_START allow 2>/dev/null; cmd appops set $targetPkg APP_AUTO_START allow 2>/dev/null; appops write-settings 2>/dev/null")
            val currentServices = session.shell("settings get secure enabled_accessibility_services").output.trim()
            val updated = HomeServices.with(currentServices, serviceComponent).joinToString(":")
            session.shell("settings put secure enabled_accessibility_services '' 2>/dev/null; settings put secure enabled_accessibility_services ${shellQuote(updated)} 2>&1")
            session.shell("settings put secure accessibility_enabled 1 2>&1")
            "Accessibility service enabled: $serviceComponent"
        }
    }

    suspend fun openLauncherOnTv(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (packageName == TvLauncherInfo.CLEAN_YOUR_TV_PKG) {
                session.shell("am start -W -n ${TvLauncherInfo.CLEAN_YOUR_TV_ACTIVITY} 2>/dev/null")
            } else {
                session.shell("monkey -p ${shellQuote(packageName)} -c android.intent.category.LAUNCHER 1 2>/dev/null || monkey -p ${shellQuote(packageName)} -c android.intent.category.HOME 1 2>/dev/null")
            }
            Unit
        }
    }

    private suspend fun writeStagedText(stagePath: String, content: String) {
        val b64 = Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        session.shell("printf '%s' '$b64' | base64 -d > $stagePath 2>/dev/null || echo '$b64' | base64 -d > $stagePath")
    }

    private suspend fun pushViaProviderOrCopy(stagePath: String, relativeName: String): Boolean {
        val targetPath = "$filesDir/$relativeName"
        // Provider write creates the file owned by CleanYourTV's UID (critical on Android 14+);
        // Android 8/9 print "[ERROR] Unsupported operation: write" with exit 0, so inspect text.
        val providerOut = session.shell(
            "rm -f $targetPath 2>/dev/null; " +
                "content write --uri $providerUri/$relativeName < $stagePath 2>&1",
        )
        val providerText = providerOut.output
        val providerSucceeded = providerOut.exitCode == 0 &&
            "Unsupported operation" !in providerText &&
            "Error while accessing provider" !in providerText &&
            "Exception" !in providerText
        if (providerSucceeded) return true

        val cpOut = session.shell("mkdir -p $filesDir 2>/dev/null; cp $stagePath $targetPath 2>&1")
        return cpOut.exitCode == 0 && "Permission denied" !in cpOut.output
    }

    private suspend fun restartHomePreservingKeyServices() {
        val savedServices = session.shell("settings get secure enabled_accessibility_services").output.trim()
        val savedEnabled = session.shell("settings get secure accessibility_enabled").output.trim()
        session.shell("am force-stop $pkg 2>/dev/null")
        if (pkg in savedServices) {
            session.shell(
                "settings put secure enabled_accessibility_services ''; " +
                    "settings put secure accessibility_enabled 0; sleep 1; " +
                    "settings put secure enabled_accessibility_services ${shellQuote(savedServices)}; " +
                    "settings put secure accessibility_enabled ${if (savedEnabled == "1") "1" else "1"}",
            )
        }
        session.shell("am start -W -a android.intent.action.MAIN -c android.intent.category.HOME -n ${TvLauncherInfo.CLEAN_YOUR_TV_ACTIVITY} 2>/dev/null || input keyevent 3")
    }
}
