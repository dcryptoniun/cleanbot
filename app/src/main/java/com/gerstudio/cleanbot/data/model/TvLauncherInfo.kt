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
 * Curated TV Launcher profile inspired by CleanYourTV and SHOWCASE.md.
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
    val accessibilityServiceComponent: String? = null,
    val directApkDownloadUrl: String? = null
) {
    companion object {
        const val CLEAN_YOUR_TV_PKG = "com.gathin.tvhome"
        const val PROJECTIVY_PKG = "com.spocky.projengmenu"
        const val FLAUNCHER_PKG = "me.efesser.flauncher"
        const val DISPATCH_PKG = "com.spauldhaliwal.dispatch"
        const val WOLF_LAUNCHER_PKG = "com.wolf.firelauncher"
        const val PRIMAL_LAUNCHER_PKG = "com.primallauncher.tv"

        val CURATED_LAUNCHERS: List<TvLauncherInfo> = listOf(
            TvLauncherInfo(
                packageName = CLEAN_YOUR_TV_PKG,
                name = "CleanYourTV Launcher",
                tagline = "Minimalist Apple TV-Style Home",
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
                tagline = "Most Powerful & Customizable",
                description = "Feature-packed launcher with customizable application channels, custom wallpapers, input shortcuts, parental controls, and built-in home button override.",
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
                description = "Clean, responsive open-source launcher built with Flutter. Categorize apps into customizable rows, hide unwanted items, and set custom wallpapers without ads.",
                developer = "Étienne Fesser",
                isCleanYourTvOfficial = false,
                isOpenSource = true,
                playStoreUrl = "https://play.google.com/store/apps/details?id=me.efesser.flauncher",
                gitHubUrl = "https://gitlab.com/efesser/flauncher",
                supportsAccessibilityRemap = false
            ),
            TvLauncherInfo(
                packageName = DISPATCH_PKG,
                name = "Dispatch Launcher",
                tagline = "Next-Gen Media Aggregator",
                description = "Modern entertainment-centric home screen that unifies Plex media, trending content, and live TV into a single streaming experience.",
                developer = "Paul Dhaliwal",
                isCleanYourTvOfficial = false,
                isOpenSource = false,
                playStoreUrl = "https://play.google.com/store/apps/details?id=com.spauldhaliwal.dispatch",
                supportsAccessibilityRemap = true,
                accessibilityServiceComponent = "com.spauldhaliwal.dispatch/com.spauldhaliwal.dispatch.services.AccessibilityService"
            ),
            TvLauncherInfo(
                packageName = WOLF_LAUNCHER_PKG,
                name = "Wolf Launcher",
                tagline = "Fire TV & Android TV Veteran",
                description = "Beloved lightweight launcher featuring adjustable banner grids, status bar toggles, custom wallpaper support, and PIN-protected apps.",
                developer = "Wolf Developer",
                isCleanYourTvOfficial = false,
                isOpenSource = false,
                supportsAccessibilityRemap = false
            ),
            TvLauncherInfo(
                packageName = PRIMAL_LAUNCHER_PKG,
                name = "Primal TV Launcher",
                tagline = "Simple Grid & Folders",
                description = "Minimalist, distraction-free grid launcher with native folder organization, banner sizing, and smooth D-Pad navigation.",
                developer = "Primal Apps",
                isCleanYourTvOfficial = false,
                isOpenSource = false,
                playStoreUrl = "https://play.google.com/store/apps/details?id=com.primallauncher.tv",
                supportsAccessibilityRemap = false
            )
        )
    }
}
