// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot

import com.gerstudio.cleanbot.data.model.CuratedDebloatPreset
import com.gerstudio.cleanbot.data.model.TvClockStyle
import com.gerstudio.cleanbot.data.model.TvDockStyle
import com.gerstudio.cleanbot.data.model.TvLauncherInfo
import com.gerstudio.cleanbot.data.model.TvTheme
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CleanYourTvFeaturesTest {

    @Test
    fun curatedThemes_allHaveValidProperties() {
        val themes = TvTheme.CURATED_THEMES
        assertTrue(themes.size >= 8)

        for (theme in themes) {
            assertTrue(theme.id.isNotEmpty())
            assertTrue(theme.name.isNotEmpty())
            assertTrue(theme.subtitle.isNotEmpty())
            assertTrue(theme.gradientColors.isNotEmpty())
            assertTrue(theme.cornerRadiusDp > 0)
            assertTrue(theme.dockOpacity in 0.5f..1.0f)
        }
    }

    @Test
    fun tvTheme_serializesToValidCleanYourTvConfigJson() {
        val aurora = TvTheme.CURATED_THEMES.first { it.id == "aurora" }
        val jsonStr = aurora.toCleanYourTvJson()
        val json = JSONObject(jsonStr)

        assertEquals("aurora", json.getString("theme_name"))
        assertEquals("Aurora Borealis", json.getString("display_name"))
        assertTrue(json.getString("accent_color").startsWith("#"))
        assertTrue(json.getString("background_tint").startsWith("#"))
        assertEquals("glass_frost", json.getString("dock_style"))
        assertEquals(1, json.getInt("schema"))
        assertTrue(json.getBoolean("tile_menu"))
        assertTrue(json.getBoolean("app_library"))
    }

    @Test
    fun oledTheme_isMarkedOledOptimized() {
        val oled = TvTheme.CURATED_THEMES.first { it.id == "oled_black" }
        assertTrue(oled.isOledOptimized)
        assertEquals(TvDockStyle.OLED_MINIMAL, oled.dockStyle)

        val json = JSONObject(oled.toCleanYourTvJson())
        assertTrue(json.getBoolean("oled_optimized"))
    }

    @Test
    fun curatedDebloatPresets_matchExpectedPackages() {
        val safePreset = CuratedDebloatPreset.PRESETS.first { it.id == "cleanyourtv_safe" }
        assertTrue(safePreset.matches("com.google.android.tvrecommendations"))
        assertTrue(safePreset.matches("com.xiaomi.mitv.analytics"))
        assertTrue(safePreset.matches("com.tcl.tv.ad"))
        assertTrue(safePreset.matches("com.hisense.tv.ad"))
        assertFalse(safePreset.matches("com.google.android.youtube"))

        val firePreset = CuratedDebloatPreset.PRESETS.first { it.id == "fire_tv_unchained" }
        assertTrue(firePreset.matches("com.amazon.tv.launcher"))
        assertTrue(firePreset.matches("com.amazon.firehomestarter"))
        assertTrue(firePreset.matches("com.amazon.device.sale.service"))
        assertFalse(firePreset.matches("com.netflix.ninja"))

        val ultraPreset = CuratedDebloatPreset.PRESETS.first { it.id == "cleanyourtv_ultra" }
        assertTrue(ultraPreset.matches("com.google.android.apps.tv.launcherx"))
        assertTrue(ultraPreset.matches("com.google.android.tvlauncher"))
    }

    @Test
    fun curatedLaunchers_includeCleanYourTvAndCommunityFavorites() {
        val launchers = TvLauncherInfo.CURATED_LAUNCHERS

        val cleanYourTv = launchers.firstOrNull { it.packageName == "com.gathin.tvhome" }
        assertNotNull(cleanYourTv)
        assertTrue(cleanYourTv!!.isCleanYourTvOfficial)
        assertTrue(cleanYourTv.isOpenSource)
        assertEquals("CleanYourTV Launcher", cleanYourTv.name)

        val projectivy = launchers.firstOrNull { it.packageName == "com.spocky.projengmenu" }
        assertNotNull(projectivy)
        assertTrue(projectivy!!.supportsAccessibilityRemap)
        assertNotNull(projectivy.accessibilityServiceComponent)

        val flauncher = launchers.firstOrNull { it.packageName == "me.efesser.flauncher" }
        assertNotNull(flauncher)
        assertTrue(flauncher!!.isOpenSource)
        assertEquals(3, launchers.size)
    }

    @Test
    fun clockStyles_haveDistinctLabels() {
        val styles = TvClockStyle.entries
        assertEquals(4, styles.size)
        val labels = styles.map { it.label }.distinct()
        assertEquals(4, labels.size)
    }

    @Test
    fun safetyClassifier_hardProtectsCoreBootAndNetworkPackages() {
        assertTrue(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("android", false))
        assertTrue(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("com.android.systemui", false))
        assertTrue(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("com.android.settings", false))
        assertTrue(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("com.android.networkstack", false))
        assertTrue(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("com.android.wifi.resources", false))
        assertTrue(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("com.android.bluetooth", false))
        assertTrue(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("com.gerstudio.cleanbot", false))
        assertTrue(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("com.google.android.apps.tv.launcherx", true))

        assertFalse(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("com.google.android.tvrecommendations", false))
        assertFalse(com.gerstudio.cleanbot.data.model.SafetyClassifier.isProtectedCore("com.xiaomi.mitv.advertise", false))
    }

    @Test
    fun tvBrandRecognizer_identifiesMajorBrandsCorrectly() {
        val sony = com.gerstudio.cleanbot.domain.engine.TvBrandRecognizer.recognize(
            device = com.gerstudio.cleanbot.data.Device(
                id = "sony1",
                hit = com.gerstudio.cleanbot.data.ScanHit("192.168.1.50", 5555, com.gerstudio.cleanbot.data.ScanKind.Classic, "BRAVIA_4K", "Sony"),
                state = com.gerstudio.cleanbot.data.DeviceState.Online,
                model = "BRAVIA 4K VH2",
                manufacturer = "Sony",
                android = "10",
                lastSeen = 1000L,
                brand = "sony"
            ),
            installedApps = emptyList()
        )
        assertEquals(com.gerstudio.cleanbot.domain.engine.TvBrandCategory.SONY_BRAVIA, sony.brand)

        val fireTv = com.gerstudio.cleanbot.domain.engine.TvBrandRecognizer.recognize(
            device = com.gerstudio.cleanbot.data.Device(
                id = "fire1",
                hit = com.gerstudio.cleanbot.data.ScanHit("192.168.1.51", 5555, com.gerstudio.cleanbot.data.ScanKind.Classic, "AFTMM", "Amazon"),
                state = com.gerstudio.cleanbot.data.DeviceState.Online,
                model = "Fire TV Stick 4K Max",
                manufacturer = "Amazon",
                android = "9",
                lastSeen = 1000L,
                brand = "amazon"
            ),
            installedApps = emptyList()
        )
        assertEquals(com.gerstudio.cleanbot.domain.engine.TvBrandCategory.FIRE_TV, fireTv.brand)
        assertEquals("fire_tv_unchained", fireTv.recommendedPreset.id)
    }
}
