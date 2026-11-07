// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Curated Debloat Presets inspired by CleanYourTV and community device lists.
 */
data class CuratedDebloatPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val packagePatterns: List<String>,
    val requiresCustomLauncherInstalled: Boolean = false
) {
    /**
     * Determines whether [packageName] matches any pattern in this preset.
     */
    fun matches(packageName: String): Boolean {
        return packagePatterns.any { pattern ->
            if (pattern.endsWith("*")) {
                packageName.startsWith(pattern.removeSuffix("*"))
            } else {
                packageName.equals(pattern, ignoreCase = true)
            }
        }
    }

    companion object {
        val PRESETS: List<CuratedDebloatPreset> = listOf(
            CuratedDebloatPreset(
                id = "cleanyourtv_safe",
                title = "CleanYourTV Safe Clean",
                subtitle = "Recommended for All TVs",
                description = "Disables sponsored ad banners, OEM tracking daemons, and background analytics while keeping casting, Google Play, audio, and all core features 100% operational.",
                icon = Icons.Default.CleaningServices,
                packagePatterns = listOf(
                    "com.google.android.tvrecommendations",
                    "com.google.android.tv.ads",
                    "com.google.android.tungsten.setupwraith",
                    "com.google.android.feedback",
                    "com.google.android.partnersetup",
                    "com.android.providers.partnerbookmarks",
                    "com.xiaomi.mitv.advertise",
                    "com.xiaomi.mitv.upgrade",
                    "com.xiaomi.mitv.analytics",
                    "com.tcl.tv.ad",
                    "com.tcl.useragreement",
                    "com.tcl.system.analytics",
                    "com.hisense.hitv.analytics",
                    "com.hisense.tv.ad",
                    "com.sony.dtv.braviasync",
                    "com.sony.dtv.browser.ceb",
                    "com.sony.dtv.customersupport",
                    "com.sony.dtv.sonyselect"
                )
            ),
            CuratedDebloatPreset(
                id = "cleanyourtv_ultra",
                title = "Ultra Clean Home",
                subtitle = "Apple TV Experience",
                description = "Strips stock ad-laden Google TV Home and recommendation overlays. Ideal when switching to CleanYourTV, Projectivy, or FLauncher for maximum TV RAM and zero ads.",
                icon = Icons.Default.Speed,
                packagePatterns = listOf(
                    "com.google.android.apps.tv.launcherx",
                    "com.google.android.tvlauncher",
                    "com.google.android.tvrecommendations",
                    "com.google.android.videos",
                    "com.google.android.play.games",
                    "com.google.android.youtube.tvunplugged",
                    "com.android.hotwordenrollment.xgoogle",
                    "com.android.hotwordenrollment.okgoogle"
                ),
                requiresCustomLauncherInstalled = true
            ),
            CuratedDebloatPreset(
                id = "fire_tv_unchained",
                title = "Fire TV Unchained",
                subtitle = "Amazon Fire OS Specific",
                description = "Removes Amazon home screen advertising carousels, sponsored screensavers, shopping background services, and telemetry for Fire TV sticks and cubes.",
                icon = Icons.Default.LocalFireDepartment,
                packagePatterns = listOf(
                    "com.amazon.tv.launcher",
                    "com.amazon.firehomestarter",
                    "com.amazon.bueller.music",
                    "com.amazon.bueller.photos",
                    "com.amazon.device.sale.service",
                    "com.amazon.advertising.idm",
                    "com.amazon.tv.support",
                    "com.amazon.device.software.ota",
                    "com.amazon.hedwig"
                ),
                requiresCustomLauncherInstalled = true
            ),
            CuratedDebloatPreset(
                id = "telemetry_killer",
                title = "Telemetry & Privacy Shield",
                subtitle = "Kill Tracking & Analytics",
                description = "Terminates background diagnostic reporting, crash telemetry, user habit analytics, and ad identification services across all TV manufacturers.",
                icon = Icons.Default.Security,
                packagePatterns = listOf(
                    "*.analytics",
                    "*.telemetry",
                    "*.metrics",
                    "*.crashdump",
                    "com.google.android.gms.ads",
                    "com.google.android.gms.measurement",
                    "com.xiaomi.mitv.tracker",
                    "com.tcl.datacenter",
                    "com.hisense.analytics"
                )
            ),
            CuratedDebloatPreset(
                id = "oem_clutter",
                title = "OEM Factory Clutter",
                subtitle = "Manufacturer Stubs & Demos",
                description = "Removes pre-installed manufacturer demo loops, promotional partner stubs, and unused default apps that take up precious internal storage.",
                icon = Icons.Default.Tv,
                packagePatterns = listOf(
                    "com.tcl.browser",
                    "com.tcl.miracast",
                    "com.hisense.tv.smartcenter",
                    "com.hisense.hitv.hicloud.account",
                    "com.mitv.screensaver",
                    "com.mitv.gallery",
                    "com.xiaomi.mitv.shop",
                    "com.sony.dtv.smartmediaapp",
                    "com.sony.dtv.demomode"
                )
            )
        )
    }
}
