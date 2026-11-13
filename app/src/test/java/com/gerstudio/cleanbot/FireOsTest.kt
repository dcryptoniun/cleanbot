// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.gerstudio.cleanbot.adb.AdbSession
import com.gerstudio.cleanbot.adb.ApkInfo
import com.gerstudio.cleanbot.adb.InstallResult
import com.gerstudio.cleanbot.adb.Progress
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.CLEAN_YOUR_TV
import com.gerstudio.cleanbot.data.CLEAN_YOUR_TV_VERSION
import com.gerstudio.cleanbot.data.CleanYourTvInstallReport
import com.gerstudio.cleanbot.data.CleanYourTvInstaller
import com.gerstudio.cleanbot.data.Device
import com.gerstudio.cleanbot.data.DeviceSource
import com.gerstudio.cleanbot.data.DeviceState
import com.gerstudio.cleanbot.data.FIRE_TV_LAUNCHER
import com.gerstudio.cleanbot.data.FakeDeviceSource
import com.gerstudio.cleanbot.data.HistoryEntry
import com.gerstudio.cleanbot.data.NotCleanYourTv
import com.gerstudio.cleanbot.data.PickedApp
import com.gerstudio.cleanbot.data.ScanHit
import com.gerstudio.cleanbot.data.ScanKind
import com.gerstudio.cleanbot.data.Snapshot
import com.gerstudio.cleanbot.data.Suggestions
import com.gerstudio.cleanbot.data.UiText
import com.gerstudio.cleanbot.data.UnsupportedTvSdkException
import com.gerstudio.cleanbot.data.isFireOs
import com.gerstudio.cleanbot.ui.CleanBotViewModel
import com.gerstudio.cleanbot.ui.CleanYourTvInstallState
import com.gerstudio.cleanbot.ui.LauncherFlow
import com.gerstudio.cleanbot.ui.PLAY_STORE
import com.gerstudio.cleanbot.ui.RECOMMENDED
import com.gerstudio.cleanbot.ui.fireInstall
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

private fun tv(manufacturer: String?, model: String?) =
    Device("tv", ScanHit("tv", 5555, ScanKind.Classic, null, null), DeviceState.Online, model, manufacturer, "11", 0L)

private val amazon = tv("Amazon", "AFTKRT")
private val insignia = tv("Insignia", "NS-43F301NA22")
private val androidTv = tv("Google", "Chromecast")

private val fireHome = AppInfo(FIRE_TV_LAUNCHER, "Fire TV Home", null, true, true, UiText(R.string.data_protected_home_screen), "1.0", home = true)
private val store = AppInfo(PLAY_STORE, "Google Play Store", null, true, true, null, "1.0")
private val tvHome = AppInfo("com.google.android.tvlauncher", "Google TV Home", null, true, true, UiText(R.string.data_protected_home_screen), "1.0", home = true)

private class FireTv(val tv: Device = amazon, val own: List<AppInfo> = listOf(fireHome)) :
    DeviceSource by FakeDeviceSource(FakeDeviceSource.Scenario.Empty, clock = { 0L }) {
    val installs = mutableListOf<ApkInfo>()
    var refuse: InstallResult? = null

    override suspend fun known() = listOf(tv)
    override fun live(deviceId: String): Flow<Snapshot> = emptyFlow()
    override suspend fun suggestions(deviceId: String, apps: suspend () -> List<AppInfo>) = Suggestions.Unavailable
    override suspend fun history(deviceId: String) = emptyList<HistoryEntry>()
    override suspend fun apps(deviceId: String) = own +
        if (installs.isEmpty()) emptyList() else listOf(
            AppInfo(CLEAN_YOUR_TV, "CleanYourTV Launcher", null, false, true, null, CLEAN_YOUR_TV_VERSION, homeInstead = FIRE_TV_LAUNCHER, homeService = true)
        )

    override suspend fun install(
        deviceId: String,
        apks: List<File>,
        apk: ApkInfo,
        allowDowngrade: Boolean,
        onProgress: (Progress) -> Unit,
    ): InstallResult {
        refuse?.let { return it }
        installs += apk
        return InstallResult.Done
    }
}

private class FakeCleanYourTvInstaller(
    var validApk: Boolean = true,
    var tvSdk: Int = 34,
) : CleanYourTvInstaller {
    var postSetupCalls = 0

    override suspend fun prepareBundled(): PickedApp {
        if (!validApk) throw NotCleanYourTv("bad checksum")
        return PickedApp(listOf(File("tvhome.apk")), ApkInfo(CLEAN_YOUR_TV, 13L, CLEAN_YOUR_TV_VERSION), "CleanYourTV Launcher")
    }

    override suspend fun openPicked(uri: Uri): PickedApp {
        if (!validApk) throw NotCleanYourTv("wrong apk")
        return PickedApp(listOf(File("tvhome.apk")), ApkInfo(CLEAN_YOUR_TV, 13L, CLEAN_YOUR_TV_VERSION), "CleanYourTV Launcher")
    }

    override suspend fun checkTvCompatibility(session: AdbSession?) {
        if (tvSdk < 26) throw UnsupportedTvSdkException(tvSdk)
    }

    override suspend fun verifyAndPostSetup(session: AdbSession?, enableKeyServices: Boolean): CleanYourTvInstallReport {
        postSetupCalls++
        return CleanYourTvInstallReport(tvSdk, launched = true, locationPermissionGranted = true, aotCompiled = true, keyServicesEnabled = enableKeyServices)
    }
}

/**
 * Bundled CleanYourTV Launcher v3.4 installation across Fire OS and Android TV: SDK check,
 * checksum verification, ADB install, post-install optimization, and picked local APK checks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FireOsTest {
    private val dispatcher = StandardTestDispatcher()
    private val test = TestScope(dispatcher)
    private var source = FireTv()
    private val fakeInstaller = FakeCleanYourTvInstaller()
    private var vm: CleanBotViewModel? = null

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() {
        vm?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    @Test fun anAmazonDeviceIsFireOs() {
        assertTrue(isFireOs(amazon, emptyList()))
        assertTrue(fireInstall(amazon, listOf(fireHome)))
    }

    @Test fun anotherMakersTvWithAmazonsLauncherIsFireOs() {
        assertTrue(isFireOs(insignia, listOf(fireHome)))
        assertFalse(isFireOs(insignia, emptyList()))
    }

    @Test fun plainAndroidTvKeepsThePlayStore() {
        assertFalse(isFireOs(androidTv, listOf(store, tvHome)))
        assertFalse(fireInstall(androidTv, listOf(store, tvHome)))
    }

    @Test fun fireOsGetsCleanYourTvWithoutAPlayStore() {
        val fire = fireInstall(amazon, listOf(fireHome))
        assertTrue(offerChoose(listOf(fireHome), fire))
        assertTrue(offerLauncher(listOf(fireHome), hidden = false, fire = fire))
        assertFalse(offerLauncher(listOf(fireHome), hidden = false))
    }

    @Test fun recommendedContainsOnlyCleanYourTvLauncher() {
        assertEquals(listOf(CLEAN_YOUR_TV), RECOMMENDED.map { it.pkg })
    }

    private fun TestScope.opened(): CleanBotViewModel {
        val v = CleanBotViewModel(source, clock = { 0L }, cleanYourTvInstaller = fakeInstaller)
        vm = v
        runCurrent()
        v.openLauncherFlow()
        return v
    }

    @Test fun installCleanYourTvChecksSdkInstallsAndRunsPostSetup() = with(test) {
        val v = opened()
        v.installCleanYourTvLauncher(enableKeyServices = true)
        runCurrent()

        assertEquals(listOf(CLEAN_YOUR_TV), source.installs.map { it.packageName })
        assertEquals(1, fakeInstaller.postSetupCalls)
        assertEquals(LauncherFlow.Step.Installed, v.state.value.device.launcher?.step)
        assertNull(v.state.value.device.launcher?.bundled)
    }

    @Test fun invalidBundledApkRefusesInstallAndShowsInvalidApk() = with(test) {
        fakeInstaller.validApk = false
        val v = opened()
        v.installCleanYourTvLauncher(enableKeyServices = true)
        runCurrent()

        assertTrue(source.installs.isEmpty())
        assertEquals(LauncherFlow.Step.Pick, v.state.value.device.launcher?.step)
        assertEquals(CleanYourTvInstallState.Phase.InvalidApk, v.state.value.device.launcher?.bundled?.phase)
    }

    @Test fun tvBelowSdk26RefusesInstallCleanly() = with(test) {
        fakeInstaller.tvSdk = 24
        val v = opened()
        v.installCleanYourTvLauncher(enableKeyServices = true)
        runCurrent()

        assertTrue(source.installs.isEmpty())
        assertEquals(CleanYourTvInstallState.Phase.Refused, v.state.value.device.launcher?.bundled?.phase)
    }

    @Test fun pickedLocalCleanYourTvApkInstallsAfterVerification() = with(test) {
        val v = opened()
        v.pickedCleanYourTvApk(Uri.parse("content://downloads/tvhome.apk"))
        runCurrent()

        assertEquals(listOf(CLEAN_YOUR_TV), source.installs.map { it.packageName })
        assertEquals(LauncherFlow.Step.Installed, v.state.value.device.launcher?.step)
    }

    @Test fun pickedNonCleanYourTvApkInstallsNothing() = with(test) {
        fakeInstaller.validApk = false
        val v = opened()
        v.pickedCleanYourTvApk(Uri.parse("content://downloads/other.apk"))
        runCurrent()

        assertTrue(source.installs.isEmpty())
        assertEquals(CleanYourTvInstallState.Phase.InvalidApk, v.state.value.device.launcher?.bundled?.phase)
    }
}
