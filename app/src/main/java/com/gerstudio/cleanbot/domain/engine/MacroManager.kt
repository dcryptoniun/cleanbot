package com.gerstudio.cleanbot.domain.engine

import com.gerstudio.cleanbot.adb.AdbShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MacroManager(private val shell: AdbShell) {

    suspend fun autoGrantProjectivyAccessibility(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val service = "com.spocky.projengmenu/com.spocky.projengmenu.services.AccessibilityService"
            shell.shell("settings put secure enabled_accessibility_services $service")
            shell.shell("settings put secure accessibility_enabled 1")
            "Projectivy Accessibility Auto-Granted"
        }
    }

    suspend fun toggleFpsOverlay(enable: Boolean): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val value = if (enable) "1" else "0"
            shell.shell("settings put global show_fps $value")
            shell.shell("setprop debug.choreographer.fps $value")
            if (enable) "FPS Overlay Enabled" else "FPS Overlay Disabled"
        }
    }

    suspend fun setTvDensity(density: Int?): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (density == null || density <= 0) {
                shell.shell("wm density reset")
                "TV Display Density Reset to Default"
            } else {
                shell.shell("wm density $density")
                "TV Display Density set to ${density}dpi"
            }
        }
    }

    suspend fun getTvDensity(): String = withContext(Dispatchers.IO) {
        val out = shell.shell("wm density").output.trim()
        out.ifEmpty { "Default" }
    }

    suspend fun trimAllAppCaches(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val out = shell.shell("pm trim-caches 999999999999999").output.trim()
            if (out.isNotEmpty()) "Caches trimmed: $out" else "All App Caches Trimmed"
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
