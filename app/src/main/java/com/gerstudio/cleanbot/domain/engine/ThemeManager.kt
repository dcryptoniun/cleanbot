// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.engine

import android.content.Context
import com.gerstudio.cleanbot.adb.AdbSession
import com.gerstudio.cleanbot.data.model.TvLauncherInfo
import com.gerstudio.cleanbot.data.model.TvTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import java.io.IOException

class ThemeManager(
    private val context: Context,
    private val session: AdbSession
) {

    /**
     * Pushes a [theme] configuration to the TV filesystem, compatible with CleanYourTV launcher
     * (`com.gathin.tvhome`) and universal wallpaper paths.
     */
    suspend fun applyThemeToTv(theme: TvTheme): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonContent = theme.toCleanYourTvJson()
            val buffer = Buffer().writeUtf8(jsonContent)

            // Primary CleanYourTV external files directory
            val primaryTargetDir = "/storage/emulated/0/Android/data/com.gathin.tvhome/files"
            val fallbackTargetDir = "/sdcard/theme"

            // Ensure directories exist
            session.shell("mkdir -p \"$primaryTargetDir\" 2>/dev/null")
            session.shell("mkdir -p \"$fallbackTargetDir\" 2>/dev/null")

            val primaryConfigPath = "$primaryTargetDir/config.json"
            val fallbackConfigPath = "$fallbackTargetDir/config.json"

            // Write through ADB shell base64 stream to avoid EACCES permission hurdles on modern Android
            val b64 = android.util.Base64.encodeToString(jsonContent.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
            session.shell("echo \"$b64\" | base64 -d > \"$primaryConfigPath\" 2>/dev/null")
            session.shell("echo \"$b64\" | base64 -d > \"$fallbackConfigPath\" 2>/dev/null")

            // Send notification intent / broadcast to reload theme if CleanYourTV is active
            session.shell("am broadcast -a com.gathin.tvhome.RELOAD_THEME 2>/dev/null")

            // Soft-refresh the current launcher
            session.shell("input keyevent 3")

            "Theme '${theme.name}' successfully pushed to TV!"
        }
    }

    /**
     * Resets any custom pushed theme back to the launcher's built-in defaults
     * (the equivalent of `tools/theme.sh --reset`).
     */
    suspend fun resetTvThemeToDefault(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            session.shell("rm -f /storage/emulated/0/Android/data/com.gathin.tvhome/files/config.json 2>/dev/null")
            session.shell("rm -rf /sdcard/theme 2>/dev/null")
            session.shell("am broadcast -a com.gathin.tvhome.RELOAD_THEME 2>/dev/null")
            session.shell("input keyevent 3")
            "TV Theme reset to default settings."
        }
    }

    /**
     * Sets [packageName] as the TV's default Home screen activity.
     */
    suspend fun setDefaultHomeLauncher(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            // First attempt via modern package manager home activity command
            val out = session.shell("cmd package set-home-activity $packageName 2>&1").output
            if ("error" in out.lowercase()) {
                // Fallback: Launch the home activity to prompt Android's launcher selector
                session.shell("monkey -p $packageName -c android.intent.category.HOME 1 2>/dev/null")
            }
            // Trigger Home button keyevent to switch
            session.shell("input keyevent 3")
            "Default Home set to $packageName"
        }
    }

    /**
     * Grants accessibility service for launchers like Projectivy, Dispatch, or CleanYourTV
     * so they can intercept and remap the TV remote's Home button seamlessly.
     */
    suspend fun grantLauncherAccessibility(serviceComponent: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val currentServices = session.shell("settings get secure enabled_accessibility_services").output.trim()
            val updated = if (currentServices.isEmpty() || currentServices == "null") {
                serviceComponent
            } else if (serviceComponent !in currentServices) {
                "$currentServices:$serviceComponent"
            } else {
                currentServices
            }

            session.shell("settings put secure enabled_accessibility_services $updated")
            session.shell("settings put secure accessibility_enabled 1")
            "Accessibility service granted for $serviceComponent"
        }
    }

    /**
     * Launches the given launcher package on the connected TV screen.
     */
    suspend fun openLauncherOnTv(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            session.shell("monkey -p $packageName -c android.intent.category.LAUNCHER 1 2>/dev/null || monkey -p $packageName -c android.intent.category.HOME 1 2>/dev/null")
            Unit
        }
    }
}
