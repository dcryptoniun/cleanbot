package com.gerstudio.cleanbot.data.model

object SafetyClassifier {

    /**
     * Hard Protected List: Core system packages essential for booting, Wi-Fi connectivity,
     * system updates, display outputs, setup wizards, input methods, Bluetooth, audio,
     * and recovery are strictly locked and can NEVER be disabled or uninstalled.
     */
    val CORE_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.providers.settings",
        "com.android.tv.settings",
        "com.google.android.tv.frameworkpackagestubs",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.shell",
        "com.android.keyguard",
        "com.android.provision",
        "com.google.android.setupwizard",
        "com.google.android.tungsten.setupwraith",
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
        "com.android.server.telecom",
        "com.android.providers.telephony",
        "com.android.hardware.input",
        "com.android.audio.service",
        "com.android.externalstorage"
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

    fun isProtectedCore(packageName: String, isCurrentLauncher: Boolean): Boolean {
        if (packageName == "com.gerstudio.cleanbot" || packageName.startsWith("com.gerstudio.cleanbot")) {
            return true
        }
        if (isCurrentLauncher) return true
        if (packageName in CORE_PACKAGES) return true
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
