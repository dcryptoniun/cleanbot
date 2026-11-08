// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data.model

/**
 * Status of a launcher on the connected TV.
 */
enum class LauncherStatus {
    ACTIVE_DEFAULT,
    INSTALLED,
    NOT_INSTALLED
}

/**
 * Curated TV Launcher profile that can be directly installed or managed from cLeanBot.
 */
data class TvLauncherInfo(
    val packageName: String,
    val name: String,
    val tagline: String,
    val description: String,
    val developer: String,
    val isCleanYourTvOfficial: Boolean = false,
    val isOpenSource: Boolean = true,
    val playStoreUrl: String? = null,
    val gitHubUrl: String? = null,
    val supportsAccessibilityRemap: Boolean = false,
    val accessibilityServiceComponent: String? = null
) {
    companion object {
        const val CLEAN_YOUR_TV_PKG = "com.gathin.tvhome"
        const val PROJECTIVY_PKG = "com.spocky.projengmenu"
        const val FLAUNCHER_PKG = "me.efesser.flauncher"

        /**
         * Only launchers that can be directly installed or opened on the TV are listed.
         */
        val CURATED_LAUNCHERS: List<TvLauncherInfo> = listOf(
            TvLauncherInfo(
                packageName = CLEAN_YOUR_TV_PKG,
                name = "CleanYourTV Launcher",
                tagline = "Clean, Fast, Zero Ads (Apple TV Style)",
                description = "Ultra-lightweight (<600 KB), zero-ad, privacy-first TV launcher. Features a floating dock, top shelf preview, and dynamic Wi-Fi theme support.",
                developer = "Gathin23 & CleanYourTV Community",
                isCleanYourTvOfficial = true,
                isOpenSource = true,
                gitHubUrl = "https://github.com/Gathin23/cleanyourtv",
                supportsAccessibilityRemap = true,
                accessibilityServiceComponent = "com.gathin.tvhome/com.gathin.tvhome.services.KeyService"
            ),
            TvLauncherInfo(
                packageName = PROJECTIVY_PKG,
                name = "Projectivy Launcher",
                tagline = "Deeply Customizable with App Channels",
                description = "Feature-packed TV launcher with customizable application rows, input shortcuts, and built-in home button override.",
                developer = "Spocky",
                isCleanYourTvOfficial = false,
                isOpenSource = false,
                playStoreUrl = "https://play.google.com/store/apps/details?id=com.spocky.projengmenu",
                gitHubUrl = "https://github.com/spocky/miproja1",
                supportsAccessibilityRemap = true,
                accessibilityServiceComponent = "com.spocky.projengmenu/com.spocky.projengmenu.services.AccessibilityService"
            ),
            TvLauncherInfo(
                packageName = FLAUNCHER_PKG,
                name = "FLauncher",
                tagline = "Pure Open Source Simplicity",
                description = "Clean, responsive open-source launcher built with Flutter. Categorize apps into custom rows, hide unwanted items, and set custom wallpapers without ads.",
                developer = "Étienne Fesser",
                isCleanYourTvOfficial = false,
                isOpenSource = true,
                playStoreUrl = "https://play.google.com/store/apps/details?id=me.efesser.flauncher",
                gitHubUrl = "https://gitlab.com/efesser/flauncher",
                supportsAccessibilityRemap = false
            )
        )
    }
}
