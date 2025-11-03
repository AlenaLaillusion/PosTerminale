package com.example.server.domain.model

data class Response (
    val status: TransactionStatus,
    val authCode: String?,
    val timestamp: Long,
    val message: String? = null
)

enum class TransactionStatus {
    APPROVED,
    DECLINED,
    TIMEOUT,
    ERROR
}