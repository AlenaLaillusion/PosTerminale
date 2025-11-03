package com.example.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test

class CryptoUtilsTest {

    private lateinit var serverKeyPair: KeyPair
    private lateinit var cryptoUtils: CryptoUtils
    private lateinit var aesKey: SecretKey

    @Before
    fun setup() {
        // Генерируем RSA ключи (имитируем сервер)
        val rsa = KeyPairGenerator.getInstance("RSA")
        rsa.initialize(2048)
        serverKeyPair = rsa.generateKeyPair()

        cryptoUtils = CryptoUtils(serverKeyPair.private)

        // Генерируем AES ключ (имитируем сессионный)
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        aesKey = keyGen.generateKey()
    }

    // --- TEST 1: RSA decryption ---
    @Test
    fun `decryptSessionKey returns same AES key`() {
        val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")

        val oaepParams = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )

        rsaCipher.init(Cipher.ENCRYPT_MODE, serverKeyPair.public, oaepParams)
        val encrypted = rsaCipher.doFinal(aesKey.encoded)

        val decrypted = cryptoUtils.decryptSessionKey(encrypted)
        assertArrayEquals(aesKey.encoded, decrypted.encoded)
    }

    // --- TEST 2: AES-GCM encryption/decryption ---
    @Test
    fun `AES GCM decrypt returns original plaintext`() {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec)

        val plaintext = "Hello Secure World!".toByteArray()
        val encrypted = cipher.doFinal(plaintext)

        val decrypted = cryptoUtils.decryptAesGcm(aesKey, iv, encrypted)

        assertArrayEquals(plaintext, decrypted)
    }

    // --- TEST 3: HMAC verification success ---
    @Test
    fun `verifyHmac returns true when HMAC matches`() {
        val data = "ImportantData".toByteArray()
        val mac = javax.crypto.Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(aesKey.encoded, "HmacSHA256"))
        val hmac = mac.doFinal(data)

        val result = cryptoUtils.verifyHmac(aesKey.encoded, data, hmac)
        assertTrue(result)
    }

    // --- TEST 4: HMAC verification failure ---
    @Test
    fun `verifyHmac returns false when HMAC does not match`() {
        val data = "ImportantData".toByteArray()
        val badHmac = ByteArray(32) { 0 }

        val result = cryptoUtils.verifyHmac(aesKey.encoded, data, badHmac)
        assertFalse(result)
    }

    // --- TEST 5: generateHmac produces deterministic result ---
    @Test
    fun `generateHmac produces consistent result`() {
        val data = "ConsistencyCheck".toByteArray()

        val hmac1 = cryptoUtils.generateHmac(aesKey, data)
        val hmac2 = cryptoUtils.generateHmac(aesKey, data)

        assertArrayEquals(hmac1, hmac2)
    }

    // --- TEST 6: AES-GCM decryption fails on wrong key ---
    @Test(expected = javax.crypto.AEADBadTagException::class)
    fun `AES GCM decryption fails with wrong key`() {
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec)

        val plaintext = "SensitiveMessage".toByteArray()
        val encrypted = cipher.doFinal(plaintext)

        // Используем другой ключ
        val otherKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        cryptoUtils.decryptAesGcm(otherKey, iv, encrypted) // ожидаем ошибку
    }
}
