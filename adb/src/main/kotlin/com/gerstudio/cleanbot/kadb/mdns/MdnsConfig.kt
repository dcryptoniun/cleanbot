package com.gerstudio.cleanbot.kadb.mdns

data class MdnsConfig(
    val serviceTypes: Set<MdnsServiceType> = MdnsServiceType.entries.toSet(),
    val preferIpv4: Boolean = true
)
