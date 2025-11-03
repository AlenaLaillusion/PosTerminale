package com.example.posterminale.domain

interface Transport {
    /**
     * Отправляет бинарный пакет на сервер и возвращает ответ (или null при ошибке).
     */
    suspend fun sendPacket(packet: ByteArray): ByteArray?
}