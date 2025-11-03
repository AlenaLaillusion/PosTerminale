package com.example.server.domain.service

import com.example.server.data.CryptoUtils
import com.example.server.data.storage.TransactionStorage
import com.example.server.domain.model.Response
import com.example.server.domain.model.TransactionStatus
import com.example.server.util.TLVBuilder
import com.example.server.util.TLVParser
import java.net.Socket
import java.time.Instant
import javax.crypto.SecretKey

/**
 * Основная бизнес-логика обработки транзакций POS → сервер.
 */
class TransactionService(
    private val cryptoUtils: CryptoUtils,
    private val storage: TransactionStorage,
    private val failureSimulator: FailureSimulator
) {

    /**
     * Обрабатывает бинарный пакет транзакции и возвращает бинарный ответ.
     */
    fun processTransactionPacket(
        socket: Socket,
        encryptedSessionKey: ByteArray,
        iv: ByteArray,
        hmac: ByteArray,
        encryptedTlvData: ByteArray
    ): ByteArray? {
        try {
            // 1. Расшифровка AES ключа (RSA-OAEP)
            val aesKey: SecretKey = cryptoUtils.decryptSessionKey(encryptedSessionKey)

            // 2. Проверка HMAC
            if (!cryptoUtils.verifyHmac(aesKey, encryptedTlvData, hmac)) {
                println("HMAC verification failed")
                return buildErrorResponse("DECLINED_INVALID_HMAC")
            }

            // 3. Расшифровка TLV данных
            val tlvBytes = cryptoUtils.decryptAesGcm(aesKey, iv, encryptedTlvData)
            val transaction = TLVParser.parseTransaction(tlvBytes)

            println("Transaction received: $transaction")

            // 4. Эмуляция результата
            val status = failureSimulator.simulate()
            if (status == TransactionStatus.TIMEOUT) {
                println("Simulated TIMEOUT for ${transaction.transactionId}")
                socket.close()
                return null
            }

            // 5. Сохраняем транзакцию
            storage.saveTransaction(transaction)

            // 6. Генерируем код авторизации
            val authCode = if (status == TransactionStatus.APPROVED)
                "%06d".format((0..999999).random())
            else null

            val response = Response(status = status, authCode = authCode,  timestamp =  System.currentTimeMillis())

            // 7. Собираем TLV-ответ
            val responseBytes = TLVBuilder.buildResponse(response)

            // (опционально можно подписать HMAC для ответа)
            return responseBytes

        } catch (e: Exception) {
            e.printStackTrace()
            return buildErrorResponse("DECLINED_INTERNAL_ERROR")
        }
    }

    private fun buildErrorResponse(reason: String): ByteArray {
        val response = Response(TransactionStatus.DECLINED, reason,  timestamp =  System.currentTimeMillis())
        return TLVBuilder.buildResponse(response)
    }
}
