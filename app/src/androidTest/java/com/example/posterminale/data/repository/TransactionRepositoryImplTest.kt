package com.example.posterminale.data.repository

import com.example.posterminale.domain.CryptoManagerContract
import com.example.posterminale.domain.Transport
import com.example.posterminale.domain.model.Transaction
import com.example.posterminale.domain.model.TransactionStatus
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.runBlocking
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import java.security.KeyPairGenerator
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class TransactionRepositoryImplTest {

  private lateinit var crypto: CryptoManagerContract
  private lateinit var transport: Transport
  private lateinit var serverPublicKey: java.security.PublicKey
  private lateinit var hmacKey: ByteArray

  private lateinit var repo: TransactionRepositoryImpl

  @Before
  fun setup() {
   crypto =  mockk<CryptoManagerContract>()
   transport = mockk<Transport>()

   val kpg = KeyPairGenerator.getInstance("RSA")
   kpg.initialize(2048)
   serverPublicKey = kpg.generateKeyPair().public

   hmacKey = ByteArray(32) { 1 }

   repo = TransactionRepositoryImpl(crypto, transport, serverPublicKey, hmacKey)
  }

  @Test
  fun sendTransactionReturnsTimeoutWhenNoResponse() = runBlocking {
   // Arrange
   val tx = Transaction("4111111111111111", 100L, "T1", "M1")

      every { crypto.generateAesKey() } returns generateAesKey()
      every { crypto.generateIv() } returns ByteArray(12)
      every { crypto.encryptAesGcm(any(), any(), any()) } returns ByteArray(8)
      every { crypto.encryptRsaOaep(any(), any()) } returns ByteArray(256)
      every { crypto.hmacSha256(any(), any()) } returns ByteArray(32)
      coEvery { transport.sendPacket(any()) } returns null // simulate timeout

   // Act
   val result = repo.sendTransaction(tx)

   // Assert
   assertEquals(TransactionStatus.TIMEOUT, result.status)
  }

  @Test
  fun SendTransactionReturnsErrorWhenResponseMalformed() = runBlocking {
   val tx = Transaction("4111111111111111", 100L, "T1", "M1")

      every { crypto.generateAesKey() } returns generateAesKey()
      every { crypto.generateIv() } returns ByteArray(12)
      every { crypto.encryptAesGcm(any(), any(), any()) } returns ByteArray(8)
      every { crypto.encryptRsaOaep(any(), any()) } returns ByteArray(256)
      every { crypto.hmacSha256(any(), any()) } returns ByteArray(32)
      coEvery { transport.sendPacket(any()) } returns ByteArray(3)

   val result = repo.sendTransaction(tx)

   assertEquals(TransactionStatus.ERROR, result.status)
  }

  private fun generateAesKey(): SecretKey {
   val kg = KeyGenerator.getInstance("AES")
   kg.init(256)
   return kg.generateKey()
  }

}