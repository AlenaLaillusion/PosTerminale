package com.example.posterminale.data.network

import android.util.Log
import com.example.posterminale.data.ApiService
import com.example.posterminale.domain.Transport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject


/**
 * Реализация Transport на базе Retrofit.
 * Подходит для HTTP/HTTPS-тестов или если нужно проксировать запросы через веб-сервер.
 */
class RetrofitClient @Inject constructor(
    private val api: ApiService
) : Transport {

    companion object {
        private const val TIMEOUT_MS = 3000L   // 3 секунды
        private const val MAX_RETRIES = 2      // максимум 2 повтора
    }

    override suspend fun sendPacket(packet: ByteArray): ByteArray? = withContext(Dispatchers.IO) {
        var attempt = 0

        while (attempt <= MAX_RETRIES) {
            try {
                attempt++
                Timber.d("Sending packet (attempt $attempt)...")

                val requestBody = packet.toRequestBody("application/octet-stream".toMediaType())

                // Используем withTimeout для жёсткого ограничения времени
                val response: Response<ResponseBody> =
                    kotlinx.coroutines.withTimeout(TIMEOUT_MS) {
                        api.sendPacket(requestBody)
                    }

                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                    Timber.d("Received ${bytes?.size ?: 0} bytes from server")
                    return@withContext bytes
                } else {
                    Timber.e("HTTP error: ${response.code()} ${response.message()}")
                }

            } catch (e: Exception) {
                when (e) {
                    is IOException -> Timber.w( "Network error: ${e.message}")
                    is kotlinx.coroutines.TimeoutCancellationException ->
                        Timber.w( "Timeout after ${TIMEOUT_MS}ms")
                    else -> Timber.e( "Unexpected error: ${e.message}", e)
                }

                // Если это не последняя попытка — подождём немного
                if (attempt <= MAX_RETRIES) {
                    delay(500)
                    Timber.d( "Retrying request ($attempt/$MAX_RETRIES)...")
                } else {
                    Timber.e( "Max retry attempts reached. Giving up.")
                    break
                }
            }
        }

        null // если ничего не получилось — возвращаем null
    }
}

