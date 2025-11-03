package com.example.posterminale.domain

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

interface CryptoManagerContract {
    fun generateAesKey(): SecretKey
    fun generateIv(): ByteArray

    @Throws(Exception::class)
    fun encryptAesGcm(plain: ByteArray, key: SecretKey, iv: ByteArray): ByteArray

    @Throws(Exception::class)
    fun decryptAesGcm(ciphertext: ByteArray, key: SecretKey, iv: ByteArray): ByteArray

    @Throws(Exception::class)
    fun encryptRsaOaep(aesKeyBytes: ByteArray, serverPublicKey: PublicKey): ByteArray

    @Throws(Exception::class)
    fun decryptRsaOaep(encrypted: ByteArray, privateKey: PrivateKey): ByteArray

    fun hmacSha256(data: ByteArray, hmacKey: ByteArray): ByteArray
    fun verifyHmac(data: ByteArray, hmacKey: ByteArray, expected: ByteArray): Boolean
}