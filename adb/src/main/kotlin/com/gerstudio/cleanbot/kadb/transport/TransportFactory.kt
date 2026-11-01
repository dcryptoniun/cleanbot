package com.gerstudio.cleanbot.kadb.transport

import com.gerstudio.cleanbot.kadb.TcpKeepAlive

internal object TransportFactory {
    suspend fun connect(host: String, port: Int, connectTimeoutMs: Long, tcpKeepAlive: TcpKeepAlive? = null): TransportChannel {
        return PlainBlockingChannel.connect(host, port, connectTimeoutMs, tcpKeepAlive)
    }
}
