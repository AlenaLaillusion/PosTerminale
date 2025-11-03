package com.example.di


import com.example.data.CryptoUtils
import com.example.data.storage.TransactionStorage
import com.example.domain.service.FailureSimulator
import com.example.domain.service.TransactionService
import com.example.util.Config
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64


object ServiceFactory {

    private val config = Config.load()

    // Синглтон для CryptoUtils
    val cryptoUtils: CryptoUtils by lazy {
        val privateKey = loadPrivateKey(config.rsaPrivateKeyPath)
        CryptoUtils(privateKey)
    }

    // Синглтон для Storage
    val storage: TransactionStorage by lazy {
        TransactionStorage()
    }

    val failureSimulator: FailureSimulator by lazy {
        FailureSimulator(
            timeoutPercent = config.timeoutPercent,
            declinePercent = config.declinePercent
        )
    }

    val transactionService: TransactionService by lazy {
        TransactionService(
            cryptoUtils = cryptoUtils,
            storage = storage,
            failureSimulator = failureSimulator
        )
    }

    val configInstance: Config
        get() = config

    private fun loadPrivateKey(path: String): PrivateKey {
        val keyBytes = Files.readAllBytes(Paths.get(path))

        // если файл начинается с "-----", значит это PEM → декодируем
        val content = String(keyBytes)
        val decoded = if (content.contains("BEGIN")) {
            Base64.getDecoder().decode(
                content
                    .replace("-----BEGIN (.*)-----".toRegex(), "")
                    .replace("-----END (.*)-----".toRegex(), "")
                    .replace("\\s".toRegex(), "")
            )
        } else {
            keyBytes
        }

        val spec = PKCS8EncodedKeySpec(decoded)
        return KeyFactory.getInstance("RSA").generatePrivate(spec)
    }
}