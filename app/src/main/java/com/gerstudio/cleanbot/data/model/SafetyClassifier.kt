package com.gerstudio.cleanbot.data.model

object SafetyClassifier {

    private val CORE_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.providers.settings",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.webview",
        "com.google.android.webview",
        "com.google.android.inputmethod.latin",
        "com.android.inputmethod.latin",
        "com.google.android.leanbackkeyboard",
        "com.android.bluetooth",
        "com.android.networkstack",
        "com.android.wifi.resources"
    )

    private val CAUTION_PACKAGES = setOf(
        "com.google.android.katniss", // Google Assistant TV
        "com.amazon.vizzini",         // Alexa TV
        "com.google.android.apps.mediashell", // Chromecast built-in
        "com.android.vending",        // Google Play Store
        "com.google.android.gms",     // Google Play Services
        "com.google.android.gsf",     // Google Services Framework
        "com.google.android.syncadapters.contacts",
        "com.sony.dtv.tvinput.cs",
        "com.tcl.tvmanager",
        "com.xiaomi.mitv.settings"
    )

    fun classify(packageName: String, isCurrentLauncher: Boolean): SafetyLevel {
        if (isCurrentLauncher) return SafetyLevel.PROTECTED_CORE
        if (packageName in CORE_PACKAGES) return SafetyLevel.PROTECTED_CORE
        if (packageName.startsWith("com.android.server") ||
            packageName.startsWith("com.android.internal") ||
            packageName.startsWith("com.google.android.networkstack")
        ) {
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
