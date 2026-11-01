package com.gerstudio.cleanbot.kadb.mdns

enum class MdnsServiceType(val dnsType: String) {
    ADB("_adb._tcp"),
    TLS_CONNECT("_adb-tls-connect._tcp"),
    TLS_PAIRING("_adb-tls-pairing._tcp")
}
