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
 *
 */

package com.gerstudio.cleanbot.kadb.pair

import com.flyfish233.crypto.spake2.Spake2Context
import com.flyfish233.crypto.spake2.Spake2Role
import org.bouncycastle.crypto.InvalidCipherTextException
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.modes.GCMBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.IllegalCharsetNameException
import java.util.Arrays
import javax.security.auth.Destroyable

// The following values are taken from the following source and are subjected to change
// https://android.googlesource.com/platform/packages/modules/adb/+/refs/heads/master/pairing_auth/pairing_auth.cpp
private val CLIENT_NAME: ByteArray
    get() = getBytes("adb pair client\u0000", "UTF-8")
private val SERVER_NAME: ByteArray
    get() = getBytes("adb pair server\u0000", "UTF-8")

// The following values are taken from the following source and are subjected to change
// https://android.googlesource.com/platform/packages/modules/adb/+/refs/heads/master/pairing_auth/aes_128_gcm.cpp
internal val INFO: ByteArray
    get() = getBytes("adb pairing_auth aes-128-gcm key", "UTF-8")

internal val HKDF_KEY_LENGTH: Int
    get() = 16

private val GCM_IV_LENGTH: Int
    get() = 12

internal class PairingAuthCtx(
    private val mSpake2Ctx: Spake2Context, password: ByteArray
) : Destroyable {
    val msg: ByteArray = mSpake2Ctx.generateMessage(password)
    private val mSecretKey = ByteArray(HKDF_KEY_LENGTH)
    private var mDecIv: Long = 0
    private var mEncIv: Long = 0
    private var mIsDestroyed = false

    fun initCipher(theirMsg: ByteArray?): Boolean {
        if (mIsDestroyed) return false
        val keyMaterial = mSpake2Ctx.processMessage(theirMsg) ?: return false
        val hkdf = HKDFBytesGenerator(SHA256Digest())
        hkdf.init(HKDFParameters(keyMaterial, null, INFO))
        hkdf.generateBytes(mSecretKey, 0, HKDF_KEY_LENGTH)
        return true
    }

    fun encrypt(input: ByteArray): ByteArray? {
        if (mIsDestroyed) return null
        return try {
            val iv = ByteBuffer.allocate(GCM_IV_LENGTH).order(ByteOrder.BIG_ENDIAN)
            iv.putLong(4, mEncIv++)
            val cipher = GCMBlockCipher.newInstance(AESEngine.newInstance())
            val params = AEADParameters(KeyParameter(mSecretKey), 128, iv.array())
            cipher.init(true, params)
            val output = ByteArray(cipher.getOutputSize(input.size))
            val len = cipher.processBytes(input, 0, input.size, output, 0)
            cipher.doFinal(output, len)
            output
        } catch (_: InvalidCipherTextException) {
            null
        }
    }

    fun decrypt(input: ByteArray): ByteArray? {
        if (mIsDestroyed) return null
        return try {
            val iv = ByteBuffer.allocate(GCM_IV_LENGTH).order(ByteOrder.BIG_ENDIAN)
            iv.putLong(4, mDecIv++)
            val cipher = GCMBlockCipher.newInstance(AESEngine.newInstance())
            val params = AEADParameters(KeyParameter(mSecretKey), 128, iv.array())
            cipher.init(false, params)
            val output = ByteArray(cipher.getOutputSize(input.size))
            val len = cipher.processBytes(input, 0, input.size, output, 0)
            cipher.doFinal(output, len)
            output
        } catch (_: InvalidCipherTextException) {
            null
        }
    }

    override fun isDestroyed(): Boolean = mIsDestroyed

    override fun destroy() {
        if (!mIsDestroyed) {
            Arrays.fill(mSecretKey, 0.toByte())
            mSpake2Ctx.destroy()
            mIsDestroyed = true
        }
    }

    companion object {
        fun createAlice(password: ByteArray): PairingAuthCtx? {
            val spake25519 = Spake2Context(Spake2Role.Alice, CLIENT_NAME, SERVER_NAME)
            return PairingAuthCtx(spake25519, password)
        }
    }
}

internal fun getBytes(text: String, charsetName: String): ByteArray {
    return try {
        text.toByteArray(charset(charsetName))
    } catch (e: UnsupportedEncodingException) {
        throw (IllegalCharsetNameException("Illegal charset $charsetName").initCause(e) as IllegalCharsetNameException)
    }
}
