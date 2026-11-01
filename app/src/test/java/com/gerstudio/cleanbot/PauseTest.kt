// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot

import androidx.lifecycle.viewModelScope
import com.gerstudio.cleanbot.adb.ConnectionRequest
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.Device
import com.gerstudio.cleanbot.data.DeviceSource
import com.gerstudio.cleanbot.data.DeviceState
import com.gerstudio.cleanbot.data.FakeDeviceSource
import com.gerstudio.cleanbot.data.NetworkInfo
import com.gerstudio.cleanbot.data.ScanEvent
import com.gerstudio.cleanbot.data.ScanHit
import com.gerstudio.cleanbot.data.ScanKind
import com.gerstudio.cleanbot.data.Snapshot
import com.gerstudio.cleanbot.data.TvAccess
import com.gerstudio.cleanbot.ui.CleanBotViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** One TV, or none with [tvs] false; every read that would reach the TV or the LAN is counted by name. */
private class Counted(private val tvs: Boolean = true) : DeviceSource by FakeDeviceSource(FakeDeviceSource.Scenario.Empty, clock = { 0L }) {
    val reads = mutableListOf<String>()
    val paused = mutableListOf<Boolean>()

    override suspend fun known() = if (!tvs) emptyList() else listOf(
        Device("tv", ScanHit("tv", 5555, ScanKind.Classic, "tv", null), DeviceState.Online, null, null, null, 0L),
    ).also { reads += "known" }

    override fun live(deviceId: String) = emptyFlow<Snapshot>().also { reads += "live" }
    override suspend fun apps(deviceId: String) = emptyList<AppInfo>().also { reads += "apps" }
    override suspend fun network(deviceId: String) = NetworkInfo("wlan0", "192.0.2.5", null, null, null, null, emptyList(), emptyList()).also { reads += "network" }
    override suspend fun connectionRequest(deviceId: String): ConnectionRequest? = null.also { reads += "prompt" }
    override suspend fun access(deviceId: String) = TvAccess().also { reads += "access" }
    override fun scan() = flowOf<ScanEvent>(ScanEvent.Done).also { reads += "scan" }

    override fun pause(paused: Boolean) {
        this.paused += paused
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PauseTest {
    private val dispatcher = StandardTestDispatcher()
    private val test = TestScope(dispatcher)
    private lateinit var vm: CleanBotViewModel

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() {
        if (::vm.isInitialized) vm.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    private fun open(tv: Counted) = with(test) {
        vm = CleanBotViewModel(tv, clock = { 0L })
        advanceTimeBy(30_000)
        runCurrent()
    }

    @Test fun inTheBackgroundNothingReadsTheTv() = with(test) {
        val tv = Counted()
        open(tv)
        assertTrue(tv.reads.containsAll(listOf("apps", "network", "prompt", "access", "known")))
        vm.pause()
        runCurrent()
        tv.reads.clear()
        advanceTimeBy(10 * 60_000)
        runCurrent()
        assertEquals(emptyList<String>(), tv.reads)
        assertEquals(listOf(true), tv.paused)
    }

    @Test fun backOnScreenThePageReadsTheTvAgain() = with(test) {
        val tv = Counted()
        open(tv)
        vm.pause()
        runCurrent()
        tv.reads.clear()
        vm.resume()
        advanceTimeBy(1_000)
        runCurrent()
        assertTrue(tv.reads.containsAll(listOf("live", "apps", "network", "prompt", "access")))
        assertEquals(listOf(true, false), tv.paused)
        assertEquals("tv", vm.state.value.selectedId)
    }

    @Test fun startingTwiceStartsOnce() = with(test) {
        val tv = Counted()
        open(tv)
        vm.resume() // the first ON_START, with nothing paused
        runCurrent()
        assertEquals(emptyList<Boolean>(), tv.paused)
    }

    @Test fun theEmptyScanStopsInTheBackgroundAndStartsAgainAfter() = with(test) {
        val tv = Counted(tvs = false)
        open(tv)
        assertTrue("scan" in tv.reads) // no TV, nothing found: it keeps looking every 10 s
        vm.pause()
        runCurrent()
        tv.reads.clear()
        advanceTimeBy(5 * 60_000)
        runCurrent()
        assertEquals(emptyList<String>(), tv.reads)
        vm.resume()
        runCurrent()
        assertEquals(listOf("scan"), tv.reads)
    }
}
