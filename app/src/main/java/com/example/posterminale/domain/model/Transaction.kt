package com.example.posterminale.domain.model

data class Transaction(
    val cardPan: String,
    val amount: Long,
    val transactionId: String,
    val merchantId: String
)
