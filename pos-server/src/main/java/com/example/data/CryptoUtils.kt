package com.example.data

import java.security.PrivateKey
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

class CryptoUtils(
    private val serverPrivateKey: PrivateKey
) {

    companion object {
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val GCM_TAG_LENGTH = 128 // bits
    }

    /**
     * Расшифровывает зашифрованный сессионный AES-ключ с помощью RSA-OAEP
     */
    fun decryptSessionKey(encryptedKey: ByteArray): SecretKey {
        val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")

        val oaepParams = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )

        rsaCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey, oaepParams)
        val aesKeyBytes = rsaCipher.doFinal(encryptedKey)
        return SecretKeySpec(aesKeyBytes, "AES")
    }

    /**
     * Проверяет корректность HMAC.
     * Возвращает true, если совпадает.
     */
    fun verifyHmac(hmacKey: ByteArray, data: ByteArray, receivedHmac: ByteArray): Boolean {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(hmacKey, "HmacSHA256")
        mac.init(keySpec)
        val calculated = mac.doFinal(data)
        return calculated.contentEquals(receivedHmac)
    }

    /**
     * Расшифровывает зашифрованные данные AES-256-GCM.
     */
    fun decryptAesGcm(aesKey: SecretKey, iv: ByteArray, encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec)
        return cipher.doFinal(encryptedData)
    }

    /**
     * Формирует HMAC для проверки (если сервер сам отправляет HMAC в ответ)
     */
    fun generateHmac(aesKey: SecretKey, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(aesKey)
        return mac.doFinal(data)
    }
}