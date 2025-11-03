package com.example.server.util

import com.example.server.domain.model.Transaction
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

/**
 * Утилита для разбора TLV-пакетов (Tag-Length-Value).
 * Используется сервером для парсинга расшифрованных данных.
 */
object TLVParser {

    /**
     * Парсит бинарный TLV в объект Transaction.
     *
     * Формат полей:
     *  0x10 - PAN (String)
     *  0x20 - Amount (Long)
     *  0x30 - Transaction ID (UUID/String)
     *  0x40 - Merchant ID (String)
     */
    fun parseTransaction(data: ByteArray): Transaction {
        val map = parse(data)

        val pan = map[0x10] ?: error("Missing PAN (0x10)")
        val amount = map[0x20]?.toLongOrNull() ?: error("Missing or invalid amount (0x20)")
        val transactionId = map[0x30] ?: UUID.randomUUID().toString()
        val merchantId = map[0x40] ?: "UNKNOWN"

        return Transaction(
            transactionId = transactionId,
            merchantId = merchantId,
            cardPan = pan,
            amount = amount,
        )
    }

    /**
     * Универсальный парсер TLV, возвращает Map<Tag, ValueString>.
     */
    fun parse(data: ByteArray): Map<Int, String> {
        val buffer = ByteBuffer.wrap(data)
        val result = mutableMapOf<Int, String>()

        while (buffer.remaining() > 0) {
            if (buffer.remaining() < 3) break // минимальный размер TLV (tag + len)

            val tag = buffer.get().toInt() and 0xFF
            val length = buffer.short.toInt() and 0xFFFF

            if (buffer.remaining() < length) break // защита от ошибок

            val valueBytes = ByteArray(length)
            buffer.get(valueBytes)

            val value = String(valueBytes, StandardCharsets.UTF_8)
            result[tag] = value
        }
        return result
    }
}