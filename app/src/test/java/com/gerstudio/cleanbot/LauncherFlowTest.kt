// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewModelScope
import com.gerstudio.cleanbot.adb.AdbSession
import com.gerstudio.cleanbot.adb.ApkInfo
import com.gerstudio.cleanbot.adb.InstallResult
import com.gerstudio.cleanbot.adb.Progress
import com.gerstudio.cleanbot.data.Action
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.CLEAN_YOUR_TV
import com.gerstudio.cleanbot.data.CLEAN_YOUR_TV_VERSION
import com.gerstudio.cleanbot.data.CleanYourTvInstallReport
import com.gerstudio.cleanbot.data.CleanYourTvInstaller
import com.gerstudio.cleanbot.data.Device
import com.gerstudio.cleanbot.data.DeviceSource
import com.gerstudio.cleanbot.data.DeviceState
import com.gerstudio.cleanbot.data.FakeDeviceSource
import com.gerstudio.cleanbot.data.HistoryEntry
import com.gerstudio.cleanbot.data.PickedApp
import com.gerstudio.cleanbot.data.ScanHit
import com.gerstudio.cleanbot.data.ScanKind
import com.gerstudio.cleanbot.data.Snapshot
import com.gerstudio.cleanbot.data.Suggestions
import com.gerstudio.cleanbot.data.UiText
import com.gerstudio.cleanbot.ui.CleanBotViewModel
import com.gerstudio.cleanbot.ui.Copy
import com.gerstudio.cleanbot.ui.LauncherActions
import com.gerstudio.cleanbot.ui.LauncherCard
import com.gerstudio.cleanbot.ui.LauncherFlow
import com.gerstudio.cleanbot.ui.LauncherScreen
import com.gerstudio.cleanbot.ui.PLAY_STORE
import com.gerstudio.cleanbot.ui.hasPlayStore
import com.gerstudio.cleanbot.ui.offerChoose
import com.gerstudio.cleanbot.ui.offerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

private const val TV_HOME = "com.google.android.apps.tv.launcherx"
private val store = AppInfo(PLAY_STORE, "Google Play Store", null, true, true, null, "1.0")
private val stock = AppInfo(TV_HOME, "Google TV Home", null, true, true, UiText(R.string.data_protected_home_screen), "1.0", home = true, builtInLauncher = true)

private class TvWithCleanYourTv : DeviceSource by FakeDeviceSource(FakeDeviceSource.Scenario.Empty, clock = { 0L }) {
    var installed = false
    var onHome = false
    var hidden = false
    val switched = mutableListOf<Pair<String, Boolean>>()

    private val device = Device("tv", ScanHit("tv", 5555, ScanKind.Classic, "tv", null), DeviceState.Online, null, null, null, 0L)

    override suspend fun known() = listOf(device)
    override fun live(deviceId: String): Flow<Snapshot> = emptyFlow()
    override suspend fun suggestions(deviceId: String, apps: suspend () -> List<AppInfo>) = Suggestions.Unavailable
    override suspend fun history(deviceId: String) = emptyList<HistoryEntry>()
    override suspend fun launcherCardHidden(deviceId: String) = hidden
    override suspend fun hideLauncherCard(deviceId: String) {
        hidden = true
    }

    override suspend fun apps(deviceId: String) = when {
        onHome -> listOf(AppInfo(CLEAN_YOUR_TV, "CleanYourTV Launcher", null, false, true, UiText(R.string.data_protected_home_screen), CLEAN_YOUR_TV_VERSION, home = true, listedLauncher = true))
        installed -> listOf(AppInfo(CLEAN_YOUR_TV, "CleanYourTV Launcher", null, false, true, null, CLEAN_YOUR_TV_VERSION, homeInstead = TV_HOME, homeService = true))
        else -> listOf(store, stock)
    }

    override suspend fun install(
        deviceId: String,
        apks: List<File>,
        apk: ApkInfo,
        allowDowngrade: Boolean,
        onProgress: (Progress) -> Unit,
    ): InstallResult {
        installed = true
        return InstallResult.Done
    }

    override suspend fun makeHome(deviceId: String, packageName: String, service: Boolean): HistoryEntry {
        switched += packageName to service
        onHome = true
        return HistoryEntry(1, deviceId, 0L, Action.Home, packageName, "CleanYourTV Launcher", Action.Home, null)
    }
}

private class TestBundledInstaller : CleanYourTvInstaller {
    override suspend fun prepareBundled() = PickedApp(
        listOf(File("tvhome.apk")),
        ApkInfo(CLEAN_YOUR_TV, 13L, CLEAN_YOUR_TV_VERSION),
        "CleanYourTV Launcher",
    )

    override suspend fun openPicked(uri: android.net.Uri) = prepareBundled()
    override suspend fun checkTvCompatibility(session: AdbSession?) = Unit
    override suspend fun verifyAndPostSetup(session: AdbSession?, enableKeyServices: Boolean) =
        CleanYourTvInstallReport(34, true, true, true, enableKeyServices)
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LauncherFlowTest {
    private val dispatcher = StandardTestDispatcher()
    private val test = TestScope(dispatcher)
    private val source = TvWithCleanYourTv()
    private lateinit var vm: CleanBotViewModel

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() {
        vm.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    private fun TestScope.started(): CleanBotViewModel {
        vm = CleanBotViewModel(source, clock = { 0L }, cleanYourTvInstaller = TestBundledInstaller())
        runCurrent()
        vm.openLauncherFlow()
        return vm
    }

    private val step get() = vm.state.value.device.launcher?.step

    @Test fun bundledInstallTransitionsToInstalledThenSwitchesHome() = with(test) {
        started().installCleanYourTvLauncher(enableKeyServices = true)
        runCurrent()
        assertTrue(source.installed)
        assertEquals(LauncherFlow.Step.Installed, step)

        vm.switchToLauncher(service = true)
        runCurrent()
        assertNull(step)
        assertEquals(listOf(CLEAN_YOUR_TV to true), source.switched)
    }

    @Test fun makeHomeDirectlySwitchesAndClosesFlow() = with(test) {
        source.installed = true
        started().makeHome(CLEAN_YOUR_TV, service = true)
        runCurrent()
        assertNull(step)
        assertEquals(listOf(CLEAN_YOUR_TV to true), source.switched)
    }

    @Test fun notNowOnInstalledSaysWhereToSwitchLater() = with(test) {
        source.installed = true
        started().launcherLater()
        assertNull(step)
        assertEquals(Copy.launcherLater("CleanYourTV Launcher"), vm.notice.value)
        assertTrue(source.switched.isEmpty())
    }

    @Test fun notNowPutsTheCardAwayForThatTv() = with(test) {
        vm = CleanBotViewModel(source, clock = { 0L })
        runCurrent()
        assertFalse(vm.state.value.device.launcherCardHidden)
        vm.hideLauncherCard()
        runCurrent()
        assertTrue(vm.state.value.device.launcherCardHidden)
        assertTrue(source.hidden)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LauncherScreenTest {
    @get:Rule val compose = createComposeRule()

    private val tv = Device("tv", ScanHit("tv", 5555, ScanKind.Classic, "tv", null), DeviceState.Online, null, null, null, 0L)

    @Test fun theCardShowsOnlyWithAStoreAndNoSecondLauncher() {
        assertTrue(offerLauncher(listOf(store, stock), hidden = false))
        assertFalse(offerLauncher(listOf(store, stock), hidden = true))
        assertFalse(offerLauncher(listOf(stock), hidden = false))
        val cleanTv = AppInfo(CLEAN_YOUR_TV, "CleanYourTV Launcher", null, false, true, null, CLEAN_YOUR_TV_VERSION, homeInstead = TV_HOME)
        assertFalse(offerLauncher(listOf(store, stock, cleanTv), hidden = false))
    }

    @Test fun anAddedLauncherOnHomeHidesCard() {
        val off = stock.copy(enabled = false, home = false)
        val cleanTv = AppInfo(CLEAN_YOUR_TV, "CleanYourTV Launcher", null, false, true, UiText(R.string.data_protected_home_screen), CLEAN_YOUR_TV_VERSION, home = true, listedLauncher = true)
        assertFalse(offerLauncher(listOf(store, off, cleanTv), hidden = false))
        assertFalse(offerChoose(listOf(store, off, cleanTv)))
        assertTrue(hasPlayStore(listOf(store, off, cleanTv)))
    }

    @Test fun theCardsTwoButtons() {
        var notNow = false
        var chose = false
        compose.setContent { LauncherCard("Google TV Home", onNotNow = { notNow = true }, onChoose = { chose = true }) }
        compose.onNodeWithText(Copy.NOT_NOW.text()).performClick()
        compose.onNodeWithText(Copy.INSTALL_CLEAN_YOUR_TV.text()).performClick()
        assertTrue(notNow && chose)
    }

    @Test fun screenOffersOneTapCleanYourTvInstallAndLocalApk() {
        val bundledCalls = mutableListOf<Boolean>()
        var picked = false
        compose.setContent {
            LauncherScreen(
                tv,
                LauncherFlow(),
                listOf(store, stock),
                LauncherActions(installBundled = { bundledCalls += it }, pickApk = { picked = true }),
            )
        }
        compose.onNodeWithText("Install CleanYourTV v$CLEAN_YOUR_TV_VERSION").performClick()
        compose.onNodeWithText("Local APK").performClick()
        assertEquals(listOf(true), bundledCalls)
        assertTrue(picked)
    }

    @Test fun installedStepActivatesCleanYourTvHome() {
        val switches = mutableListOf<Boolean>()
        val cleanTv = AppInfo(CLEAN_YOUR_TV, "CleanYourTV Launcher", null, false, true, null, CLEAN_YOUR_TV_VERSION, homeInstead = TV_HOME, homeService = true)
        compose.setContent {
            LauncherScreen(
                tv,
                LauncherFlow(LauncherFlow.Step.Installed, CLEAN_YOUR_TV),
                listOf(store, stock, cleanTv),
                LauncherActions(switch = { switches += it }),
            )
        }
        compose.onNodeWithText("Activate CleanYourTV Home").performClick()
        assertEquals(listOf(true), switches)
    }
}
