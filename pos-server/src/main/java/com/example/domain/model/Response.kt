package com.example.domain.model

data class Response (
    val status: TransactionStatus,
    val authCode: String?,
    val timestamp: Long
)

enum class TransactionStatus {
    APPROVED,
    DECLINED,
    TIMEOUT,
    ERROR
}