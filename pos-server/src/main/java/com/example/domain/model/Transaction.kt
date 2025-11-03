package com.example.domain.model

data class Transaction(
    val cardPan: String,
    val amount: Long,
    val transactionId: String,
    val merchantId: String
)
