// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.engine

import com.gerstudio.cleanbot.adb.AdbShell
import com.gerstudio.cleanbot.adb.HomeServices
import com.gerstudio.cleanbot.data.model.TvLauncherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MacroManager(private val shell: AdbShell) {

    /**
     * Enables CleanYourTV's remote key helpers (`InputKeyService` / `HomeKeyService`), including
     * TCL `AUTO_START` / `APP_AUTO_START` app-ops per `launcher/build-and-install.sh`, without
     * clobbering other accessibility services already active on the TV.
     */
    suspend fun enableCleanYourTvKeyServices(
        homeKey: Boolean = true,
        inputKey: Boolean = true,
        grantRestorePermission: Boolean = false,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val pkg = TvLauncherInfo.CLEAN_YOUR_TV_PKG
            shell.shell("cmd appops set $pkg AUTO_START allow 2>/dev/null; cmd appops set $pkg APP_AUTO_START allow 2>/dev/null; appops write-settings 2>/dev/null")
            var current = shell.shell("settings get secure enabled_accessibility_services").output.trim()
            val targets = buildList {
                if (homeKey) add(TvLauncherInfo.CLEAN_YOUR_TV.homeKeyServiceComponent)
                if (inputKey) add(TvLauncherInfo.CLEAN_YOUR_TV.inputKeyServiceComponent)
            }
            for (svc in targets) {
                current = HomeServices.with(current, svc).joinToString(":")
            }
            if (current.isNotEmpty()) {
                shell.shell("settings put secure enabled_accessibility_services '' 2>/dev/null; settings put secure enabled_accessibility_services '$current' 2>&1")
                shell.shell("settings put secure accessibility_enabled 1 2>&1")
            }
            if (grantRestorePermission) {
                shell.shell("pm grant $pkg android.permission.WRITE_SECURE_SETTINGS 2>/dev/null")
            }
            "CleanYourTV remote key services enabled"
        }
    }

    suspend fun readFpsOverlay(): Boolean = withContext(Dispatchers.IO) {
        val prop = shell.shell("getprop debug.choreographer.fps").output.trim()
        val global = shell.shell("settings get global show_fps").output.trim()
        prop == "1" || global == "1"
    }

    suspend fun toggleFpsOverlay(enable: Boolean): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val value = if (enable) "1" else "0"
            val r1 = shell.shell("settings put global show_fps $value 2>&1")
            val r2 = shell.shell("setprop debug.choreographer.fps $value 2>&1")
            if ("Exception" in r1.output || "SecurityException" in r2.output) {
                error(r1.output.ifBlank { r2.output }.trim())
            }
            if (enable) "FPS Overlay Enabled" else "FPS Overlay Disabled"
        }
    }

    suspend fun setTvDensity(density: Int?): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cmd = if (density == null || density <= 0) "wm density reset 2>&1" else "wm density $density 2>&1"
            val r = shell.shell(cmd)
            if (r.exitCode != 0 || "Error" in r.output || "Exception" in r.output) {
                error(r.output.trim().ifEmpty { "Could not update TV display density" })
            }
            if (density == null || density <= 0) "TV Display Density Reset to Default" else "TV Display Density set to ${density}dpi"
        }
    }

    suspend fun getTvDensity(): String = withContext(Dispatchers.IO) {
        val out = shell.shell("wm density").output.trim()
        out.ifEmpty { "Default" }
    }

    suspend fun trimAllAppCaches(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val r = shell.shell("pm trim-caches 999G 2>&1")
            if (r.exitCode != 0 || "Exception" in r.output || "Error" in r.output) {
                error(r.output.trim().ifEmpty { "TV refused cache trim" })
            }
            "All App Caches Trimmed"
        }
    }

    companion object {
        suspend fun sendWakeOnLan(macAddress: String, broadcastIp: String = "255.255.255.255"): Result<Unit> = withContext(Dispatchers.IO) {
            runCatching {
                val cleanMac = macAddress.replace(":", "").replace("-", "")
                require(cleanMac.length == 12) { "Invalid MAC address: $macAddress" }

                val macBytes = ByteArray(6)
                for (i in 0 until 6) {
                    macBytes[i] = cleanMac.substring(i * 2, i * 2 + 2).toInt(16).toByte()
                }

                val packetBytes = ByteArray(6 + 16 * macBytes.size)
                for (i in 0 until 6) {
                    packetBytes[i] = 0xFF.toByte()
                }
                for (i in 0 until 16) {
                    System.arraycopy(macBytes, 0, packetBytes, 6 + i * macBytes.size, macBytes.size)
                }

                val address = InetAddress.getByName(broadcastIp)
                val packet = DatagramPacket(packetBytes, packetBytes.size, address, 9)
                DatagramSocket().use { socket ->
                    socket.broadcast = true
                    socket.send(packet)
                }
            }
        }
    }
}
