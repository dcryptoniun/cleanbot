package com.gerstudio.cleanbot.kadb.cert

interface KadbPrivateKeyStore {
    fun readPrivateKeyPem(): ByteArray?
    fun writePrivateKeyPemAtomic(privateKeyPem: ByteArray)

    fun clear()
}
