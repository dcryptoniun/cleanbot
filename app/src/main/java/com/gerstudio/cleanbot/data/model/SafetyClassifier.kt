// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data.model

object SafetyClassifier {

    /**
     * Hard Protected List aligned with cleanyourtv `tools/disable.sh` (`CORE_RE` minus `CORE_OK`)
     * plus essential input/Bluetooth/networking packages that must never be disabled or uninstalled.
     */
    val CORE_PACKAGES = setOf(
        "android",
        "android.ext.services",
        "android.ext.shared",
        "com.android.sdksandbox",
        "com.android.location.fused",
        "com.android.systemui",
        "com.android.shell",
        "com.android.phone",
        "com.android.settings",
        "com.android.providers.settings",
        "com.android.tv.settings",
        "com.google.android.tv.frameworkpackagestubs",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.ext.services",
        "com.google.android.ext.services",
        "com.android.ext.shared",
        "com.google.android.ext.shared",
        "com.android.modulemetadata",
        "com.google.android.modulemetadata",
        "com.android.keyguard",
        "com.android.provision",
        "com.google.android.setupwizard",
        "com.google.android.onetimeinitializer",
        "com.android.webview",
        "com.google.android.webview",
        "com.google.android.inputmethod.latin",
        "com.android.inputmethod.latin",
        "com.google.android.leanbackkeyboard",
        "com.android.bluetooth",
        "com.android.bluetoothmidiservice",
        "com.google.android.apps.tv.remote.service",
        "com.android.networkstack",
        "com.android.networkstack.tethering",
        "com.google.android.networkstack",
        "com.google.android.networkstack.tethering",
        "com.android.wifi.resources",
        "com.google.android.wifi.resources",
        "com.android.server.telecom",
        "com.android.providers.telephony",
        "com.android.hardware.input",
        "com.android.audio.service",
        "com.android.externalstorage",
        // MediaTek / MatrixTV TV middleware guarded by cleanyourtv (#1578)
        "com.cltv.mal",
        "com.mediatek.tis",
        "com.mediatek.tv.service",
        "com.mediatek.tv.agent",
    )

    private val CORE_PROVIDERS_OK = setOf(
        "com.android.providers.calendar",
        "com.android.providers.contacts",
        "com.android.providers.userdictionary",
        "com.android.providers.blockednumber",
    )

    private val CAUTION_PACKAGES = setOf(
        "com.google.android.katniss", // Google Assistant TV
        "com.google.android.tungsten.setupwraith", // Google TV setup wizard (guard Wi-Fi owner before disabling!)
        "com.amazon.vizzini", // Alexa TV
        "com.google.android.apps.mediashell", // Chromecast built-in
        "com.android.vending", // Google Play Store
        "com.google.android.gms", // Google Play Services
        "com.google.android.gsf", // Google Services Framework
        "com.google.android.syncadapters.contacts",
        "com.sony.dtv.tvinput.cs",
        "com.tcl.tvmanager",
        "com.xiaomi.mitv.settings",
    )

    fun isProtectedCore(packageName: String, isCurrentLauncher: Boolean): Boolean {
        if (packageName == "com.gerstudio.cleanbot" || packageName.startsWith("com.gerstudio.cleanbot")) {
            return true
        }
        if (isCurrentLauncher) return true
        if (packageName in CORE_PACKAGES) return true
        if (packageName.startsWith("com.android.providers.") && packageName !in CORE_PROVIDERS_OK) return true
        if (packageName.startsWith("com.google.android.overlay.modules")) return true
        if (packageName.startsWith("com.android.server") ||
            packageName.startsWith("com.android.internal") ||
            packageName.startsWith("com.google.android.networkstack") ||
            packageName.startsWith("com.android.networkstack")
        ) {
            return true
        }
        return false
    }

    fun classify(packageName: String, isCurrentLauncher: Boolean): SafetyLevel {
        if (isProtectedCore(packageName, isCurrentLauncher)) {
            return SafetyLevel.PROTECTED_CORE
        }
        if (packageName in CAUTION_PACKAGES) return SafetyLevel.CAUTION
        if (packageName.contains("assistant") ||
            packageName.contains("cast") ||
            packageName.contains("launcher") ||
            packageName.contains("settings")
        ) {
            return SafetyLevel.CAUTION
        }
        return SafetyLevel.SAFE_TO_REMOVE
    }
}
