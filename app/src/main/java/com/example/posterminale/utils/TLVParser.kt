package com.example.posterminale.utils

import com.example.posterminale.domain.model.Transaction
import com.example.posterminale.domain.model.TransactionResult
import com.example.posterminale.domain.model.TransactionStatus
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
    fun parseTransaction(data: ByteArray): TransactionResult {
        val map = parse(data)

        val pan = map[0x10] ?: error("Missing PAN (0x10)")
        val authCode = map[0x20] ?: error("Missing or invalid authCode (0x20)")
        val timestamp = map[0x30]
            ?.trim()
            ?.toLongOrNull() // пробуем преобразовать строку в Long
            ?: System.currentTimeMillis() // если нет — берём текущее время

        return TransactionResult(
            status = when (pan) {
                "APPROVED" -> TransactionStatus.APPROVED
                "DECLINED" -> TransactionStatus.DECLINED
                "TIMEOUT" -> TransactionStatus.TIMEOUT
               else ->  TransactionStatus.APPROVED
            },
            authCode = authCode,
            timestamp = timestamp,

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
            result[tag] = value.replace("\u0000", "")
        }
        return result
    }
}