package com.example.server.util

import com.example.server.domain.model.Response
import com.example.server.domain.model.Transaction
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

/**
 * Утилита для сборки TLV-пакетов (Tag-Length-Value).
 * Используется для формирования ответов сервера.
 */
object TLVBuilder {

    /**
     * Строит TLV-пакет из объекта Response.
     */
    fun buildResponse(response: Response): ByteArray {
        val output = ByteArrayOutputStream()

        // TAG 0x10 — STATUS
        output.writeTag(0x10, response.status.name)

        // TAG 0x20 — AUTH_CODE (если есть)
        response.authCode?.let {
            output.writeTag(0x20, it)
        }

        // TAG 0x30 — TIMESTAMP
        output.writeTag(0x30, response.timestamp.toString())

        return output.toByteArray()
    }

    /**
     * Строит TLV-пакет из объекта Transaction (для тестов или логов).
     */
    fun buildTransaction(transaction: Transaction): ByteArray {
        val output = ByteArrayOutputStream()
        output.writeTag(0x10, transaction.cardPan)
        output.writeTag(0x20, transaction.amount.toString())
        output.writeTag(0x30, transaction.transactionId)
        output.writeTag(0x40, transaction.merchantId)
       // output.writeTag(0x50, transaction.timestamp.toString())
        return output.toByteArray()
    }

    // --- Extension-функция для записи TLV ---
    private fun ByteArrayOutputStream.writeTag(tag: Int, value: String) {
        val valueBytes = value.toByteArray(StandardCharsets.UTF_8)
        val lengthBytes = ByteBuffer.allocate(2).putShort(valueBytes.size.toShort()).array()
        this.write(tag)
        this.write(lengthBytes)
        this.write(valueBytes)
    }
}