package com.gerstudio.cleanbot.kadb.cert.platform

fun defaultDeviceName(software: String? = null): String {
    software
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { return it }

    val userName = System.getProperty("user.name")
        ?: System.getenv("USER")
    val hostName = System.getenv("COMPUTERNAME")
        ?: System.getenv("HOSTNAME")
        ?: "cLeanBot"
    return formatDefaultDeviceName(userName, hostName)
}

internal fun formatDefaultDeviceName(loginName: String?, hostName: String?): String {
    fun normalize(component: String?): String {
        return component
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "unknown"
    }

    return "${normalize(loginName)}@${normalize(hostName)}"
}
