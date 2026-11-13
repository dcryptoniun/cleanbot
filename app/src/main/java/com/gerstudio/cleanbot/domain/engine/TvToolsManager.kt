// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.engine

import com.gerstudio.cleanbot.adb.AdbSession
import com.gerstudio.cleanbot.adb.shellQuote
import com.gerstudio.cleanbot.data.model.TvLauncherInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * TV Sleep Timer status.
 */
data class SleepTimerState(
    val active: Boolean = false,
    val totalMinutes: Int = 0,
    val remainingSeconds: Int = 0,
)

class TvToolsManager(
    private val session: AdbSession,
    private val scope: CoroutineScope,
) {
    private var sleepTimerJob: Job? = null
    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    /**
     * Starts a sleep timer for [minutes]. When expired, puts the TV to sleep over ADB.
     */
    fun startSleepTimer(minutes: Int) {
        val safeMinutes = minutes.coerceAtLeast(1)
        sleepTimerJob?.cancel()
        _sleepTimerState.value = SleepTimerState(
            active = true,
            totalMinutes = safeMinutes,
            remainingSeconds = safeMinutes * 60,
        )

        sleepTimerJob = scope.launch(Dispatchers.Default) {
            var remaining = safeMinutes * 60
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _sleepTimerState.value = _sleepTimerState.value.copy(remainingSeconds = remaining)
            }
            withContext(Dispatchers.IO) {
                runCatching { session.shell("input keyevent 223 2>/dev/null || input keyevent 26") }
            }
            _sleepTimerState.value = SleepTimerState()
        }
    }

    /**
     * Cancels any active sleep timer.
     */
    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerState.value = SleepTimerState()
    }

    /**
     * Sends display sleep (`KEYCODE_SLEEP`), keeping background playback / ADB active where supported.
     */
    suspend fun toggleAudioOnlyScreenOff(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val r = session.shell("input keyevent 223 2>&1")
            if (r.exitCode != 0 || "Error" in r.output) {
                error(r.output.trim().ifEmpty { "TV refused sleep keyevent" })
            }
            "Display sleep / Audio-only command sent to TV"
        }
    }

    /**
     * Starts CleanYourTV's Clock screensaver if installed, else Android's system Somnambulator/Backdrop.
     */
    suspend fun triggerScreensaver(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val candidates = listOf(
                "am start -W -n ${TvLauncherInfo.CLEAN_YOUR_TV_PKG}/.ClockActivity 2>&1",
                "am start -W -n com.android.systemui/.Somnambulator 2>&1",
                "am start -W -n com.google.android.backdrop/.BackdropActivity 2>&1",
            )
            for (cmd in candidates) {
                val res = session.shell(cmd)
                if (res.exitCode == 0 && "Status: ok" in res.output && "Error" !in res.output) {
                    return@runCatching "Screensaver started on TV"
                }
            }
            error("No compatible screensaver activity responded on this TV")
        }
    }

    /**
     * Trims all app caches on the TV to reclaim internal storage.
     */
    suspend fun trimCaches(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val r = session.shell("pm trim-caches 999G 2>&1")
            if (r.exitCode != 0 || "Exception" in r.output || "Error" in r.output) {
                error(r.output.trim().ifEmpty { "TV refused cache trim" })
            }
            "All App Caches Trimmed"
        }
    }

    /**
     * Sets display density (DPI) on the TV. Pass null to reset to default.
     */
    suspend fun setDensity(dpi: Int?): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val cmd = if (dpi == null || dpi <= 0) "wm density reset 2>&1" else "wm density $dpi 2>&1"
            val r = session.shell(cmd)
            if (r.exitCode != 0 || "Error" in r.output || "Exception" in r.output) {
                error(r.output.trim().ifEmpty { "Could not update TV display density" })
            }
            if (dpi == null || dpi <= 0) "TV Display Density reset to default" else "TV Display Density set to ${dpi}dpi"
        }
    }

    /**
     * Reads current TV density.
     */
    suspend fun getDensity(): String = withContext(Dispatchers.IO) {
        session.shell("wm density").output.trim().ifEmpty { "Default" }
    }

    /**
     * Executes reboot commands: Normal Reboot, Recovery, or Soft System Restart.
     */
    suspend fun executeReboot(mode: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            when (mode) {
                "recovery" -> {
                    val r = session.shell("svc power reboot recovery 2>&1 || reboot recovery 2>&1")
                    if (r.exitCode != 0 && "Permission" in r.output) error(r.output.trim())
                    "Rebooting to Recovery..."
                }
                "soft" -> {
                    val r = session.shell("am restart 2>&1")
                    if (r.exitCode == 0 && "Exception" !in r.output && "Permission" !in r.output) {
                        "Soft UI restart initiated"
                    } else {
                        session.reboot()
                        "TV Restarting..."
                    }
                }
                else -> {
                    session.reboot()
                    "TV Rebooting..."
                }
            }
        }
    }

    /**
     * Disables wireless ADB debugging / USB debugging safely on standard non-root TVs,
     * and resets tcpip port if rooted/allowed.
     */
    suspend fun secureAdbPort(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val r = session.shell(
                "settings put global adb_wifi_enabled 0 2>&1; " +
                    "settings put global adb_enabled 0 2>&1; " +
                    "setprop service.adb.tcp.port -1 2>/dev/null",
            )
            if ("SecurityException" in r.output) {
                error("TV refused turning off ADB over shell: ${r.output.trim()}")
            }
            "ADB debugging disabled. Re-enable in TV Developer options when needed."
        }
    }
}
