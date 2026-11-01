package com.gerstudio.cleanbot.kadb.mdns

import kotlinx.coroutines.flow.StateFlow

interface KadbMdns : AutoCloseable {
    val state: StateFlow<MdnsDiscoveryState>

    fun start()

    fun stop()

    override fun close() = stop()
}
