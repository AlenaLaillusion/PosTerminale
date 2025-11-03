package com.example.domain.service

import com.example.domain.model.TransactionStatus
import kotlin.random.Random

/**
 * Отвечает за эмуляцию сбоев и отказов:
 *  - 5% запросов → TIMEOUT
 *  - 3% запросов → DECLINED
 *  - Остальные → APPROVED
 */
class FailureSimulator(
    private val timeoutPercent: Int = 5,
    private val declinePercent: Int = 3
) {

    /**
     * Возвращает статус транзакции согласно вероятностному распределению.
     */
    fun simulate(): TransactionStatus {
        val rand = Random.nextInt(100)
        return when {
            rand < timeoutPercent -> TransactionStatus.TIMEOUT
            rand < timeoutPercent + declinePercent -> TransactionStatus.DECLINED
            else -> TransactionStatus.APPROVED
        }
    }
}