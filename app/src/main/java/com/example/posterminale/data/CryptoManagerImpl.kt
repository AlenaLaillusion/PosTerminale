package com.example.posterminale.data

import com.example.posterminale.domain.CryptoManagerContract
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManagerImpl @Inject constructor(
    private val secureRandom: SecureRandom // injected via Hilt
) : CryptoManagerContract {

    companion object {
        private const val TAG = "CryptoManager"
        private const val AES_ALGO = "AES"
        private const val AES_TRANSFORM = "AES/GCM/NoPadding"
        private const val RSA_TRANSFORM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        private const val HMAC_ALGO = "HmacSHA256"
        private const val GCM_TAG_LEN = 128 // bits
        private const val AES_KEY_BITS = 256
        private const val IV_BYTES = 12
    }

    override fun generateAesKey(): SecretKey {
        val kg = KeyGenerator.getInstance(AES_ALGO)
        kg.init(AES_KEY_BITS, secureRandom)
        return kg.generateKey()
    }

    override fun generateIv(): ByteArray {
        val iv = ByteArray(IV_BYTES)
        secureRandom.nextBytes(iv)
        return iv
    }

    override fun encryptAesGcm(plain: ByteArray, key: SecretKey, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_TRANSFORM)
        val spec = GCMParameterSpec(GCM_TAG_LEN, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        return cipher.doFinal(plain)
    }

    override fun decryptAesGcm(ciphertext: ByteArray, key: SecretKey, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_TRANSFORM)
        val spec = GCMParameterSpec(GCM_TAG_LEN, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(ciphertext)
    }

    override fun encryptRsaOaep(data: ByteArray, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")

        val oaepParams = OAEPParameterSpec(
            "SHA-256",                      // digest
            "MGF1",                         // mask generation
            MGF1ParameterSpec.SHA256,       // mgf digest
            PSource.PSpecified.DEFAULT
        )

        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
        return cipher.doFinal(data)
    }

    override fun decryptRsaOaep(encrypted: ByteArray, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance(RSA_TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(encrypted)
    }

    override fun hmacSha256(data: ByteArray, hmacKey: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGO)
        val keySpec = SecretKeySpec(hmacKey, HMAC_ALGO)
        mac.init(keySpec)
        return mac.doFinal(data)
    }

    override fun verifyHmac(data: ByteArray, hmacKey: ByteArray, expected: ByteArray): Boolean {
        val actual = hmacSha256(data, hmacKey)
        return MessageDigest.isEqual(actual, expected)
    }

    // helper: load PublicKey from X.509 bytes (if you store key in config as base64)
    fun loadPublicKey(encoded: ByteArray): PublicKey {
        val spec = X509EncodedKeySpec(encoded)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(spec)
    }
}