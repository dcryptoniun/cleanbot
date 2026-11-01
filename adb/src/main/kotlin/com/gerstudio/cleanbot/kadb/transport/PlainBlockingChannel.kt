/*
 * Copyright (c) 2024 Flyfish-Xu
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gerstudio.cleanbot.kadb.transport

import com.gerstudio.cleanbot.kadb.TcpKeepAlive
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.math.min

internal class PlainBlockingChannel private constructor(
    private val socket: Socket
) : TransportChannel {

    companion object {
        fun connect(host: String, port: Int, connectTimeoutMs: Long, tcpKeepAlive: TcpKeepAlive? = null): PlainBlockingChannel {
            require(connectTimeoutMs >= 0) { "Connect timeout must not be negative" }
            val socket = Socket()
            try {
                socket.keepAlive = true
                socket.tcpNoDelay = true
                socket.connect(InetSocketAddress(host, port), connectTimeoutMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                tcpKeepAlive?.let { configureTcpKeepAlive(socket, it) }
                return PlainBlockingChannel(socket)
            } catch (error: Throwable) {
                runCatching { socket.close() }
                throw error
            }
        }
    }

    private val input get() = socket.getInputStream()
    private val output get() = socket.getOutputStream()

    override val localAddress: InetSocketAddress
        get() = socket.localSocketAddress as InetSocketAddress

    override val remoteAddress: InetSocketAddress
        get() = socket.remoteSocketAddress as InetSocketAddress

    override val isOpen: Boolean
        get() = socket.isConnected && !socket.isClosed

    override suspend fun read(dst: ByteBuffer, timeout: Long, unit: TimeUnit): Int {
        val remaining = dst.remaining()
        if (remaining == 0) return 0
        val timeoutMs = unit.toMillis(timeout).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val originalSoTimeout = socket.soTimeout
        try {
            socket.soTimeout = timeoutMs
            val buffer = ByteArray(min(remaining, 8192))
            val read = input.read(buffer, 0, buffer.size)
            if (read > 0) {
                dst.put(buffer, 0, read)
            }
            return read
        } finally {
            try {
                socket.soTimeout = originalSoTimeout
            } catch (_: Throwable) {
            }
        }
    }

    override suspend fun write(src: ByteBuffer, timeout: Long, unit: TimeUnit): Int {
        val remaining = src.remaining()
        if (remaining == 0) return 0
        val timeoutMs = unit.toMillis(timeout).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val originalSoTimeout = socket.soTimeout
        try {
            socket.soTimeout = timeoutMs
            val count = min(remaining, 8192)
            val buffer = ByteArray(count)
            src.get(buffer)
            output.write(buffer, 0, count)
            output.flush()
            return count
        } finally {
            try {
                socket.soTimeout = originalSoTimeout
            } catch (_: Throwable) {
            }
        }
    }

    override suspend fun readExactly(dst: ByteBuffer, timeout: Long, unit: TimeUnit) {
        while (dst.hasRemaining()) {
            val n = read(dst, timeout, unit)
            if (n < 0) throw java.io.EOFException("EOF while readExactly")
        }
    }

    override suspend fun writeExactly(src: ByteBuffer, timeout: Long, unit: TimeUnit) {
        while (src.hasRemaining()) {
            write(src, timeout, unit)
        }
    }

    override suspend fun shutdownInput() {
        if (!socket.isInputShutdown) socket.shutdownInput()
    }

    override suspend fun shutdownOutput() {
        if (!socket.isOutputShutdown) socket.shutdownOutput()
    }

    override fun close() {
        socket.close()
    }
}

internal fun configureTcpKeepAlive(socket: Socket, policy: TcpKeepAlive) {
    socket.keepAlive = true
    try {
        val buildVersionClass = Class.forName("android.os.Build\$VERSION")
        val sdkInt = buildVersionClass.getField("SDK_INT").getInt(null)
        if (sdkInt >= 29) {
            val pfdClass = Class.forName("android.os.ParcelFileDescriptor")
            val fromSocketMethod = pfdClass.getMethod("fromSocket", Socket::class.java)
            val pfd = fromSocketMethod.invoke(null, socket) as? AutoCloseable
            pfd?.use {
                val getFdMethod = pfdClass.getMethod("getFileDescriptor")
                val fd = getFdMethod.invoke(pfd)
                val osClass = Class.forName("android.system.Os")
                val setsockoptIntMethod = osClass.getMethod(
                    "setsockoptInt",
                    Class.forName("java.io.FileDescriptor"),
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                setsockoptIntMethod.invoke(null, fd, 6, 4, policy.idleSeconds)
                setsockoptIntMethod.invoke(null, fd, 6, 5, policy.intervalSeconds)
                setsockoptIntMethod.invoke(null, fd, 6, 6, policy.probeCount)
            }
        }
    } catch (_: Throwable) {
        // Fallback for JVM or older Android versions
    }
}
