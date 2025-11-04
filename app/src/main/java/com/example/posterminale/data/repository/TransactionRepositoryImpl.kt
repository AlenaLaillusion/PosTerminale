package com.example.posterminale.data.repository

import android.content.Context
import com.example.posterminale.data.TlvEncoder
import com.example.posterminale.data.network.PacketBuilder
import com.example.posterminale.data.network.TcpClient
import com.example.posterminale.domain.CryptoManagerContract
import com.example.posterminale.domain.Transport
import com.example.posterminale.domain.model.Transaction
import com.example.posterminale.domain.model.TransactionResult
import com.example.posterminale.domain.model.TransactionStatus
import com.example.posterminale.domain.repository.TransactionRepository
import com.example.posterminale.utils.TLVParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.PublicKey
import javax.inject.Inject
import javax.inject.Named

class TransactionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crypto: CryptoManagerContract,
   // private val tcpClient: TcpClient,
    private val transport: Transport,
    @Named("ServerPublicKey") private val serverPublicKey: PublicKey,
    @Named("HmacKey") private val hmacKey: ByteArray
) : TransactionRepository {

    override suspend fun sendTransaction(transaction: Transaction): TransactionResult {
        val aesKey = crypto.generateAesKey()
        val iv = crypto.generateIv()
        val tlvData = TlvEncoder.encodeTransaction(transaction)

        val encryptedPayload = crypto.encryptAesGcm(tlvData, aesKey, iv)
        val encryptedSessionKey = crypto.encryptRsaOaep(aesKey.encoded, serverPublicKey)
        val hmac = crypto.hmacSha256(encryptedPayload, hmacKey)

        val packet = PacketBuilder.build(
            version = 0x01,
            type = 0x01,
            encryptedSessionKey = encryptedSessionKey,
            iv = iv,
            hmac = hmac,
            encryptedTlv = encryptedPayload
        )

        val response = transport.sendPacket(packet)

        val file = File(context.filesDir, "packet.bin")
        file.writeBytes(packet)
        return parseServerResponse(response)
    }

    private fun parseServerResponse(response: ByteArray?): TransactionResult {
        if (response == null || response.isEmpty()) {
            return TransactionResult(
                status = TransactionStatus.TIMEOUT,
                authCode = null,
                timestamp = System.currentTimeMillis()
            )
        }

        return try {
            val buffer = ByteBuffer.wrap(response).order(ByteOrder.BIG_ENDIAN)
            val tlvParse = TLVParser.parseTransaction(response)

            val status = tlvParse.status
            val authCode = tlvParse.authCode

            val timestamp = tlvParse.timestamp

            TransactionResult(
                status = tlvParse.status,
                authCode = tlvParse.authCode,
                timestamp = tlvParse.timestamp,
            )
        } catch (e: Exception) {
            TransactionResult(
                status = TransactionStatus.ERROR,
                authCode = null,
                timestamp = System.currentTimeMillis(),
            )
        }
    }
}