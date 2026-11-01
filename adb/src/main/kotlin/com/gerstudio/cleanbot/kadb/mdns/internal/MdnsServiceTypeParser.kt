package com.gerstudio.cleanbot.kadb.mdns.internal

import com.gerstudio.cleanbot.kadb.mdns.MdnsServiceType

fun parseMdnsServiceType(serviceType: String?): MdnsServiceType? {
    val normalized = serviceType.normalizeMdnsServiceType()
    return MdnsServiceType.entries.firstOrNull { it.dnsType == normalized }
}

fun String?.normalizeMdnsServiceType(): String =
    this.orEmpty()
        .trim()
        .removeSuffix(".")
        .removeSuffix(".local")
        .lowercase()
