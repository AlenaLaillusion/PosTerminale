package com.example.posterminale

import android.util.Log
import android.util.Log.*
import com.example.posterminale.data.ApiService
import com.example.posterminale.data.network.RetrofitClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


class EndToEndRetrofitTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: ApiService
    private lateinit var client: RetrofitClient

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        api = retrofit.create(ApiService::class.java)
        client = RetrofitClient(api)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    // Сервер отвечает 200 (успешно)
    @Test
    fun sendPacketReturnsBytesWhenServerResponds200() = runBlocking {
        val expected = "hello".toByteArray()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(okio.Buffer().write(expected))
        )

        val result = client.sendPacket("ping".toByteArray())

        assertNotNull(result)
        assertArrayEquals(expected, result)
    }

    // Сервер отвечает 500 — клиент повторяет
    @Test
    fun sendPacketRetriesOnHttpError() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        val result = client.sendPacket("ping".toByteArray())

        assertNotNull(result)
        assertArrayEquals("ok".toByteArray(), result)
        assertEquals(2, mockWebServer.requestCount)
    }

    // Сервер "висит" — срабатывает таймаут
    @Test
    fun sendPacketReturnsNullAfterTimeout() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("slow")
                .setBodyDelay(11, java.util.concurrent.TimeUnit.SECONDS)
        )

        val start = System.currentTimeMillis()
        val result = client.sendPacket("ping".toByteArray())
        val elapsed = System.currentTimeMillis() - start

        assertNull(result)
        assertTrue(elapsed < 11000)
    }

    // Сетевая ошибка (IOException)
    @Test
    fun sendPacket_returnsNull_onNetworkFailure() = runBlocking {
        mockWebServer.shutdown()

        val result = client.sendPacket("ping".toByteArray())

        assertNull(result)
    }

    // Проверка количества попыток через spy ApiService
    @Test
    fun sendPacket_retriesUpToMaxRetries_onFailure() = runBlocking {
        val mockApi = mockk<ApiService>()
        val clientWithMock = RetrofitClient(mockApi)

        coEvery { mockApi.sendPacket(any()) } throws java.io.IOException("Network down")

        val result = clientWithMock.sendPacket("ping".toByteArray())

        coVerify (exactly = 3) { mockApi.sendPacket(any()) } // 1 + 2 повтора
        assertNull(result)
    }
}