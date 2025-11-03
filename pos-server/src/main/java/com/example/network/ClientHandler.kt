package com.example.network

import com.example.di.ServiceFactory
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class ClientHandler(
    private val socket: Socket,
    private val serviceFactory: ServiceFactory
) {

    fun handle() {
        val input = DataInputStream(socket.getInputStream())
        val output = DataOutputStream(socket.getOutputStream())

        try {
            // Пример чтения пакета (упрощённый)
            val header = ByteArray(4)
            input.readFully(header)

            val encryptedSessionKey = ByteArray(256)
            input.readFully(encryptedSessionKey)

            val iv = ByteArray(12)
            input.readFully(iv)

            val hmac = ByteArray(32)
            input.readFully(hmac)

            val remaining = input.available()
            val encryptedData = ByteArray(remaining)
            input.readFully(encryptedData)

            // Обработка транзакции
            val response = serviceFactory.transactionService.processTransactionPacket(
                socket, encryptedSessionKey, iv, hmac, encryptedData
            )

            // Ответ клиенту
            if (response != null) {
                output.write(response)
                output.flush()
                println("Response sent to client")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            socket.close()
        }
    }
}
