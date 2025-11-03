package com.example.posterminale.domain.model


data class TransactionResult(
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
