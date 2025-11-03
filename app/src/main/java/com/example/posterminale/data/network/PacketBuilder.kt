package com.example.posterminale.data.network

import java.nio.ByteBuffer
import java.nio.ByteOrder

object PacketBuilder {

    private const val HEADER_SIZE = 4
    private const val RSA_ENCRYPTED_KEY_SIZE = 256   // зашифрованный AES ключ RSA
    private const val IV_SIZE = 12                   // AES-GCM IV
    private const val HMAC_SIZE = 32                 // SHA-256 HMAC

    /**
     * Собирает финальный бинарный пакет
     */
    fun build(
        version: Int,
        type: Int,
        encryptedSessionKey: ByteArray,   // 256 байт
        iv: ByteArray,                    // 12 байт
        hmac: ByteArray,                  // 32 байта
        encryptedTlv: ByteArray           // зашифрованные данные TLV
    ): ByteArray {

        require(encryptedSessionKey.size == RSA_ENCRYPTED_KEY_SIZE) {
            "ENCRYPTED_SESSION_KEY must be 256 bytes"
        }
        require(iv.size == IV_SIZE) { "IV must be 12 bytes" }
        require(hmac.size == HMAC_SIZE) { "HMAC must be 32 bytes" }

        // Вычисляем общую длину пакета
        val totalLength = HEADER_SIZE +
                RSA_ENCRYPTED_KEY_SIZE +
                IV_SIZE +
                HMAC_SIZE +
                encryptedTlv.size

        // HEADER: [1 байт версия][1 байт тип][2 байта длина]

        val header = ByteBuffer.allocate(HEADER_SIZE)
            .order(ByteOrder.BIG_ENDIAN)
            .put(version.toByte())
            .put(type.toByte())
            .putShort(totalLength.toShort())
            .array()

        // Собираем весь пакет
        val packet = ByteBuffer.allocate(totalLength)
            .order(ByteOrder.BIG_ENDIAN)
            .put(header)
            .put(encryptedSessionKey)
            .put(iv)
            .put(hmac)
            .put(encryptedTlv)
            .array()

        return packet
    }
}
