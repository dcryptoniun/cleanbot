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
 * Curated debloat presets derived from Gathin23/cleanyourtv examples and universal TV rules.
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
                description = "Disables sponsored ad rows, Google & OEM telemetry, partner auto-installers, and feedback daemons while keeping casting, Play Store, audio, and HDMI inputs 100% intact.",
                icon = Icons.Default.CleaningServices,
                packagePatterns = listOf(
                    "com.google.android.tvrecommendations",
                    "com.google.android.tv.ads",
                    "com.google.android.feedback",
                    "com.google.android.partnersetup",
                    "com.android.providers.partnerbookmarks",
                    "android.autoinstalls.config.*",
                    "com.xiaomi.mitv.advertise",
                    "com.xiaomi.mitv.upgrade",
                    "com.xiaomi.mitv.analytics",
                    "com.tcl.tv.ad",
                    "com.tcl.bootadservice",
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
                subtitle = "Switch to CleanYourTV Launcher",
                description = "Safely freezes stock ad-heavy home launchers once CleanYourTV Launcher is installed and active, reclaiming RAM and removing sponsored rows.",
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
                title = "Fire TV Clean",
                subtitle = "Amazon Fire OS Specific",
                description = "Freezes Amazon home ad banners, sponsored screensavers, shopping daemons, and ACR telemetry on Fire TV sticks and editions.",
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
                requiresCustomLauncherInstalled = false
            ),
            CuratedDebloatPreset(
                id = "telemetry_killer",
                title = "Telemetry & Privacy Shield",
                subtitle = "Stop Tracking & ACR",
                description = "Stops ACR watchers, diagnostic loggers, crash reporters, user habit analytics, and ad-ID daemons across TV makers.",
                icon = Icons.Default.Security,
                packagePatterns = listOf(
                    "*.analytics",
                    "*.telemetry",
                    "*.metrics",
                    "*.crashdump",
                    "com.google.android.gms.ads",
                    "com.google.android.gms.measurement",
                    "com.google.android.adservices.api",
                    "com.xiaomi.mitv.tracker",
                    "com.tcl.datacenter",
                    "com.hisense.analytics",
                    "com.anoki.fast"
                )
            ),
            CuratedDebloatPreset(
                id = "oem_clutter",
                title = "OEM Factory Clutter",
                subtitle = "Retail Demos & Preloads",
                description = "Removes retail demo loops, promotional partner stubs, and unused bundled junk to reclaim TV storage.",
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
