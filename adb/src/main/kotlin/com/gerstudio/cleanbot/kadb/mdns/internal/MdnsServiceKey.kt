package com.gerstudio.cleanbot.kadb.mdns.internal

import com.gerstudio.cleanbot.kadb.mdns.MdnsServiceType

data class MdnsServiceKey(
    val name: String,
    val serviceType: MdnsServiceType
)
