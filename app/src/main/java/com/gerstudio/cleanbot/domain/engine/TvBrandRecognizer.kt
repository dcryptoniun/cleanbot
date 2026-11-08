// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.engine

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.Device
import com.gerstudio.cleanbot.data.model.CuratedDebloatPreset

enum class TvBrandCategory(
    val displayName: String,
    val icon: ImageVector,
    val recommendedPresetId: String
) {
    GOOGLE_TV("Google TV", Icons.Default.Cast, "cleanyourtv_safe"),
    SONY_BRAVIA("Sony Bravia", Icons.Default.Tv, "cleanyourtv_safe"),
    XIAOMI_MITV("Xiaomi Mi TV", Icons.Default.Tv, "cleanyourtv_safe"),
    TCL("TCL Android TV", Icons.Default.Tv, "cleanyourtv_safe"),
    FIRE_TV("Amazon Fire TV", Icons.Default.LocalFireDepartment, "fire_tv_unchained"),
    NVIDIA_SHIELD("Nvidia Shield TV", Icons.Default.Speed, "cleanyourtv_safe"),
    HISENSE("Hisense Android TV", Icons.Default.Tv, "cleanyourtv_safe"),
    PHILIPS("Philips Android TV", Icons.Default.Tv, "cleanyourtv_safe"),
    GENERIC_ANDROID_TV("Android TV", Icons.Default.Tv, "cleanyourtv_safe")
}

data class RecognizedTvProfile(
    val brand: TvBrandCategory,
    val modelName: String,
    val osVersion: String,
    val currentLauncherPackage: String?,
    val currentLauncherName: String,
    val isCleanYourTvActive: Boolean,
    val isCleanYourTvInstalled: Boolean,
    val recommendedPreset: CuratedDebloatPreset
)

object TvBrandRecognizer {

    fun recognize(device: Device?, installedApps: List<AppInfo>): RecognizedTvProfile {
        val brandLower = (device?.brand ?: "").lowercase()
        val manufacturerLower = (device?.manufacturer ?: "").lowercase()
        val modelLower = (device?.model ?: "").lowercase()
        val hostnameLower = (device?.hit?.hostname ?: "").lowercase()
        val makerLower = (device?.hit?.maker ?: "").lowercase()

        val allTokens = "$brandLower $manufacturerLower $modelLower $hostnameLower $makerLower"

        val category = when {
            "amazon" in allTokens || "firetv" in allTokens || "fire tv" in allTokens || "aft" in allTokens -> TvBrandCategory.FIRE_TV
            "sony" in allTokens || "bravia" in allTokens -> TvBrandCategory.SONY_BRAVIA
            "xiaomi" in allTokens || "mitv" in allTokens || "redmi" in allTokens -> TvBrandCategory.XIAOMI_MITV
            "tcl" in allTokens || "beyondtv" in allTokens || "uniontv" in allTokens -> TvBrandCategory.TCL
            "shield" in allTokens || "nvidia" in allTokens || "foster" in allTokens || "darcy" in allTokens -> TvBrandCategory.NVIDIA_SHIELD
            "hisense" in allTokens -> TvBrandCategory.HISENSE
            "philips" in allTokens || "tpv" in allTokens -> TvBrandCategory.PHILIPS
            "google" in allTokens || "chromecast" in allTokens || "sabrina" in allTokens || "boreal" in allTokens -> TvBrandCategory.GOOGLE_TV
            else -> TvBrandCategory.GENERIC_ANDROID_TV
        }

        val activeLauncher = installedApps.firstOrNull { it.home }
        val activeLauncherPkg = activeLauncher?.packageName
        val activeLauncherName = activeLauncher?.label ?: "Stock System Launcher"

        val cleanYourTvApp = installedApps.firstOrNull { it.packageName == "com.gathin.tvhome" }
        val isCleanYourTvInstalled = cleanYourTvApp != null
        val isCleanYourTvActive = cleanYourTvApp?.home == true

        val modelName = device?.nickname
            ?: device?.model
            ?: device?.hit?.hostname
            ?: device?.hit?.maker
            ?: category.displayName

        val osVersion = if (device?.android != null) "Android TV ${device.android}" else "Android TV OS"

        val recommendedPreset = CuratedDebloatPreset.PRESETS.firstOrNull { it.id == category.recommendedPresetId }
            ?: CuratedDebloatPreset.PRESETS.first()

        return RecognizedTvProfile(
            brand = category,
            modelName = modelName,
            osVersion = osVersion,
            currentLauncherPackage = activeLauncherPkg,
            currentLauncherName = activeLauncherName,
            isCleanYourTvActive = isCleanYourTvActive,
            isCleanYourTvInstalled = isCleanYourTvInstalled,
            recommendedPreset = recommendedPreset
        )
    }
}
