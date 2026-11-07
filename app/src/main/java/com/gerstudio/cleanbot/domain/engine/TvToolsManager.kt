// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.engine

import com.gerstudio.cleanbot.adb.AdbSession
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
    val remainingSeconds: Int = 0
)

class TvToolsManager(
    private val session: AdbSession,
    private val scope: CoroutineScope
) {
    private var sleepTimerJob: Job? = null
    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState.asStateFlow()

    /**
     * Starts a sleep timer for [minutes]. When expired, wirelessly turns off the TV.
     */
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerState.value = SleepTimerState(
            active = true,
            totalMinutes = minutes,
            remainingSeconds = minutes * 60
        )

        sleepTimerJob = scope.launch(Dispatchers.Default) {
            var remaining = minutes * 60
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _sleepTimerState.value = _sleepTimerState.value.copy(remainingSeconds = remaining)
            }
            // Timer expired: trigger TV standby
            withContext(Dispatchers.IO) {
                session.shell("input keyevent 223 || input keyevent 26")
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
     * Toggles Audio-Only mode (turns off screen backlight or sleeps panel while keeping audio).
     */
    suspend fun toggleAudioOnlyScreenOff(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            session.shell("cmd activity broadcast -a com.android.server.action.SET_DISPLAY_STATE --ei state 0 2>/dev/null || input keyevent 223")
            "Screen off / Audio-only standby activated"
        }
    }

    /**
     * Triggers the TV's ambient screensaver / daydream immediately.
     */
    suspend fun triggerScreensaver(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            session.shell("cmd activity broadcast -a android.intent.action.DREAMING_STARTED 2>/dev/null || am start -n com.google.android.backdrop/.BackdropActivity 2>/dev/null")
            "Ambient screensaver triggered"
        }
    }

    /**
     * Trims all app caches on the TV to reclaim flash storage.
     */
    suspend fun trimCaches(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val out = session.shell("pm trim-caches 999999999999999").output.trim()
            if (out.isNotEmpty()) "Caches trimmed: $out" else "All App Caches Trimmed"
        }
    }

    /**
     * Sets display density (DPI) on the TV. Pass null to reset to default.
     */
    suspend fun setDensity(dpi: Int?): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (dpi == null || dpi <= 0) {
                session.shell("wm density reset")
                "TV Display Density reset to default"
            } else {
                session.shell("wm density $dpi")
                "TV Display Density set to ${dpi}dpi"
            }
        }
    }

    /**
     * Reads current TV density.
     */
    suspend fun getDensity(): String = withContext(Dispatchers.IO) {
        val out = session.shell("wm density").output.trim()
        out.ifEmpty { "Default" }
    }

    /**
     * Executes reboot commands: Quick Restart, Soft Restart, or Recovery.
     */
    suspend fun executeReboot(mode: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            when (mode) {
                "soft" -> {
                    session.shell("setprop ctl.restart zygote 2>/dev/null || pkill -f system_server 2>/dev/null")
                    "Soft Restart initiated"
                }
                "recovery" -> {
                    session.shell("reboot recovery 2>/dev/null")
                    "Rebooting to Recovery"
                }
                else -> {
                    session.shell("reboot 2>/dev/null")
                    "TV Rebooting"
                }
            }
        }
    }

    /**
     * Securely closes the TV's wireless ADB port when optimization is finished.
     */
    suspend fun secureAdbPort(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            session.shell("setprop service.adb.tcp.port -1 2>/dev/null && stop adbd 2>/dev/null && start adbd 2>/dev/null")
            "Wireless ADB port secured and closed."
        }
    }
}
