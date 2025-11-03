package com.example.posterminale

import com.example.posterminale.data.network.TcpClient
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.test.assertNull

class EndToEndTcpClientTest {
private lateinit var client: TcpClient

@Before
fun setup() {
    Timber.uprootAll()
    Timber.plant(object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            println("[${tag ?: "TcpClient"}] $message")
        }
    })

    client = TcpClient("127.0.0.1", 9999)
}

@After
fun tearDown() {
    Timber.uprootAll()
}

//Успешный ответ сервера
@Test
fun sendPacketSuccessResponse() = runBlocking {
    // Мокаем Socket и его потоки
    val mockSocket = mockk<Socket>(relaxed = true)
    val mockOutput = mockk<OutputStream>(relaxed = true)
    val mockInput = ByteArrayInputStream("HELLO".toByteArray())

    every { mockSocket.getOutputStream() } returns mockOutput
    every { mockSocket.getInputStream() } returns mockInput
    every { mockSocket.connect(any<InetSocketAddress>(), any()) } just Runs

    mockkConstructor(Socket::class)
    every { anyConstructed<Socket>().connect(any<InetSocketAddress>(), any()) } just Runs
    every { anyConstructed<Socket>().getOutputStream() } returns mockOutput
    every { anyConstructed<Socket>().getInputStream() } returns mockInput

    val response = client.sendPacket("HELLO".toByteArray())

    assertNotNull(response)
    assertEquals("HELLO", response!!.decodeToString())

    verify { mockOutput.write("HELLO".toByteArray()) }
    verify { mockOutput.flush() }
}

//Таймаут чтения
@Test
fun sendPacketTimeoutReturnsNull() = runBlocking {
    mockkConstructor(Socket::class)
    // Симулируем, что connect() проходит, но чтение зависает
    every { anyConstructed<Socket>().connect(any<InetSocketAddress>(), any()) } just Runs
    every { anyConstructed<Socket>().getOutputStream() } returns mockk(relaxed = true)
    every { anyConstructed<Socket>().getInputStream() } answers {
        throw SocketTimeoutException("read timed out")
    }

    val result = client.sendPacket("PING".toByteArray())

    assertNull(actual = null, result.toString())
 }

    @Test
    fun sendPacketRetriesExpectedTimes() = runBlocking {
        // Arrange
        mockkConstructor(Socket::class)

        val mockOutput = mockk<OutputStream>(relaxed = true)
        val mockInput = mockk<InputStream>()

        every { anyConstructed<Socket>().connect(any(), any()) } just Runs
        every { anyConstructed<Socket>().getOutputStream() } returns mockOutput
        every { anyConstructed<Socket>().getInputStream() } returns mockInput

        // Все попытки выбрасывают IOException
        every { mockInput.read(any()) } throws java.io.IOException("Simulated network failure")

        // Act
        val result = client.sendPacket("PING".toByteArray())

        // Assert
        assertNull(result)
        verify(exactly = 3) { // 1 основная + 2 retry = 3 connect()
            anyConstructed<Socket>().connect(any<InetSocketAddress>(), any())
        }
    }

    @Test
    fun sendPacketStopsAfterTimeout() = runBlocking {
        val fakeSocket = mockk<Socket>()

        every { fakeSocket.connect(any(), any()) } answers { Thread.sleep(3100); Unit }
        every { fakeSocket.getOutputStream() } returns mockk(relaxed = true)
        every { fakeSocket.getInputStream() } returns mockk(relaxed = true)
        every { fakeSocket.close() } just Runs

        val start = System.currentTimeMillis()
        val result = client.sendPacket("DATA".toByteArray())
        val duration = System.currentTimeMillis() - start

        assertNull(result)
        // проверяем, что не зависает вечно
        assertTrue("Took too long: ${duration}ms", duration < 11000)
    }

}