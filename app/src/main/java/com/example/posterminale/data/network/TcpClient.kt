package com.example.posterminale.data.network

import android.util.Log
import com.example.posterminale.domain.Transport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Named


class TcpClient @Inject constructor(
    @Named("tcpHost") private val host: String,
    @Named("tcpPort") private val port: Int,
    private val socketFactory: () -> Socket = { Socket() } //для unit-тестов
) : Transport {

    private val timeoutMs = 3000
    private val maxRetries = 2

    override suspend fun sendPacket(packet: ByteArray): ByteArray? = withContext(Dispatchers.IO) {
        var attempt = 0
        var lastError: Exception? = null

        while (attempt <= maxRetries) {
            try {
                Timber.d("Attempt ${attempt + 1}: connecting to $host:$port")

                val socket = socketFactory()
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                socket.soTimeout = timeoutMs

                val out: OutputStream = socket.getOutputStream()
                val input: InputStream = socket.getInputStream()

                out.write(packet)
                out.flush()

                val response = input.readBytes()

                input.close()
                out.close()
                socket.close()

                Timber.d("Response received (${response.size} bytes)")
                return@withContext response
            } catch (e: Exception) {
                lastError = e
                Timber.e("Error sending TCP packet: ${e.message}", e)

                attempt++
                if (attempt <= maxRetries) {
                    Timber.d( "Retrying ($attempt/$maxRetries)...")
                    delay(500L)
                }
            }
        }

        Timber.e("All attempts failed: ${lastError?.message}")
        return@withContext null
    }
}

