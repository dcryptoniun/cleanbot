// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data.model

/**
 * Status of the CleanYourTV launcher on the connected TV.
 */
enum class LauncherStatus {
    ACTIVE_DEFAULT,
    INSTALLED,
    NOT_INSTALLED
}

/**
 * Official TV launcher built directly from Gathin23/cleanyourtv.
 */
data class TvLauncherInfo(
    val packageName: String,
    val name: String,
    val versionName: String,
    val versionCode: Int,
    val minSdk: Int,
    val tagline: String,
    val description: String,
    val developer: String,
    val assetApkName: String,
    val assetSha256: String,
    val isCleanYourTvOfficial: Boolean = true,
    val isOpenSource: Boolean = true,
    val gitHubUrl: String = "https://github.com/Gathin23/cleanyourtv",
    val supportsAccessibilityRemap: Boolean = true,
    val homeKeyServiceComponent: String = "com.gathin.tvhome/.HomeKeyService",
    val inputKeyServiceComponent: String = "com.gathin.tvhome/.InputKeyService",
    val accessibilityServiceComponent: String = "com.gathin.tvhome/.HomeKeyService",
) {
    companion object {
        const val CLEAN_YOUR_TV_PKG = "com.gathin.tvhome"
        const val CLEAN_YOUR_TV_ACTIVITY = "com.gathin.tvhome/.MainActivity"
        const val CLEAN_YOUR_TV_ASSET = "tvhome.apk"
        const val CLEAN_YOUR_TV_SHA256 = "baf6c79b7a8dc6dfa3722ac4c151595229651b3ad93bee54f00f1c126122e967"

        val CLEAN_YOUR_TV = TvLauncherInfo(
            packageName = CLEAN_YOUR_TV_PKG,
            name = "CleanYourTV Launcher",
            versionName = "3.4",
            versionCode = 13,
            minSdk = 26,
            tagline = "Zero Ads · Instant Home · Built-in Themes",
            description = "Latest v3.4 open-source TV home screen (~586 KB) bundled straight from Gathin23/cleanyourtv. Replaces stock ad carousels with a clean dock, app library, HDMI inputs pill, and wireless theme overlays.",
            developer = "Gathin23 & CleanYourTV Contributors (MIT)",
            assetApkName = CLEAN_YOUR_TV_ASSET,
            assetSha256 = CLEAN_YOUR_TV_SHA256,
        )

        /**
         * Only the latest CleanYourTV Launcher from Gathin23/cleanyourtv is offered.
         */
        val CURATED_LAUNCHERS: List<TvLauncherInfo> = listOf(CLEAN_YOUR_TV)
    }
}
