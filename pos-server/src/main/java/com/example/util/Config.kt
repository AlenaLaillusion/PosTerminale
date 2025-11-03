package com.example.util

import java.io.File
import java.util.Properties

/*
 * Конфигурация POS Server.
 * Загружается из application.properties или задаётся значениями по умолчанию.
 */
data class Config(
    val port: Int,
    val rsaPrivateKeyPath: String,
    val dbPath: String,
    val timeoutPercent: Int,
    val declinePercent: Int
) {
    companion object {
        private const val CONFIG_FILE = "application.properties"

        fun load(): Config {
            val props = Properties()

            // Пробуем загрузить конфиг из файла (если есть)
            val configFile = File(CONFIG_FILE)
            if (configFile.exists()) {
                props.load(configFile.inputStream())
            } else {
                println("Config file not found, using defaults.")
            }

            return Config(
                port = props.getProperty("server.port", "5000").toInt(),
                rsaPrivateKeyPath = props.getProperty("crypto.privateKeyPath", "keys/private_key.pem"),
                dbPath = props.getProperty("storage.dbPath", "data/transactions.db"),
                timeoutPercent = props.getProperty("failure.timeoutPercent", "5").toInt(),
                declinePercent = props.getProperty("failure.declinePercent", "3").toInt()
            )
        }
    }
}
