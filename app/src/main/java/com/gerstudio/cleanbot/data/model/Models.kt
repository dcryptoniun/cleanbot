package com.gerstudio.cleanbot.data.model

import kotlinx.serialization.Serializable

enum class PackageActionMode {
    DISABLE,
    UNINSTALL_USER_0,
    UNINSTALL_USER_0_KEEP_DATA
}

enum class SafetyLevel {
    SAFE_TO_REMOVE,
    CAUTION,
    PROTECTED_CORE
}

@Serializable
data class TvProfile(
    val id: String,
    val name: String,
    val ip: String,
    val port: Int = 5555,
    val macAddress: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val isWirelessDebugging: Boolean = false,
    val lastConnected: Long = 0L
)

@Serializable
data class DebloatPreset(
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val packages: List<DebloatItem>
)

@Serializable
data class DebloatItem(
    val packageName: String,
    val mode: String, // "DISABLE" or "UNINSTALL"
    val keepData: Boolean = false,
    val note: String = ""
)

@Serializable
enum class LedgerActionType {
    DISABLED,
    UNINSTALLED_USER_0,
    UNINSTALLED_USER_0_KEEP,
    STOPPED,
    INSTALLED_EXISTING,
    ENABLED
}

@Serializable
data class DebloatLedgerEntry(
    val deviceId: String,
    val packageName: String,
    val appLabel: String,
    val actionType: LedgerActionType,
    val timestamp: Long = System.currentTimeMillis(),
    val wasSystemApp: Boolean = true
)
