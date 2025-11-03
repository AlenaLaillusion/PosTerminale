package com.example.server.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.server.data.CryptoUtils
import com.example.server.data.storage.TransactionStorage
import com.example.server.domain.service.FailureSimulator
import com.example.server.domain.service.TransactionService
import com.example.server.util.Config
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec

@RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadPrivateKey(path: String): PrivateKey {
        val keyBytes = Files.readAllBytes(Paths.get(path))
        val spec = PKCS8EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(spec)
    }
}
