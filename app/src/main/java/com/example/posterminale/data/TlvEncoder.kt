package com.example.posterminale.data

import com.example.posterminale.domain.model.Transaction
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets


object TlvEncoder {

    private const val TAG_PAN: Byte = 0x10
    private const val TAG_AMOUNT: Byte = 0x20
    private const val TAG_TRANSACTION_ID: Byte = 0x30
    private const val TAG_MERCHANT_ID: Byte = 0x40


    fun encodeTransaction(transaction: Transaction): ByteArray {
        val tlvList = mutableListOf<ByteArray>()

        tlvList += encodeField(TAG_PAN, transaction.cardPan.toByteArray(StandardCharsets.UTF_8))
        tlvList += encodeField(TAG_AMOUNT, encodeAmount(transaction.amount))
        tlvList += encodeField(TAG_TRANSACTION_ID, transaction.transactionId.toByteArray(StandardCharsets.UTF_8))
        tlvList += encodeField(TAG_MERCHANT_ID, transaction.merchantId.toByteArray(StandardCharsets.UTF_8))

        return tlvList.reduce { acc, bytes -> acc + bytes }
    }

    private fun encodeField(tag: Byte, value: ByteArray): ByteArray {
        val lengthBytes = ByteBuffer.allocate(2)
            .order(ByteOrder.BIG_ENDIAN)  // или Middle-Endian если указано в задании
            .putShort(value.size.toShort())
            .array()
        return byteArrayOf(tag) + lengthBytes + value
    }

    /**
     * Перевод суммы в байты (Middle-Endian как указано в задании)
     */
    fun encodeAmount(amount: Long): ByteArray {
        val bytes = ByteBuffer.allocate(8).putLong(amount).array()
        return bytes.reversedArray() // Middle-Endian (перевёрнутый порядок)
    }
}