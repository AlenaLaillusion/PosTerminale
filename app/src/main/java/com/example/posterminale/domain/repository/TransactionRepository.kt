package com.example.posterminale.domain.repository

import com.example.posterminale.domain.model.Transaction
import com.example.posterminale.domain.model.TransactionResult

interface TransactionRepository {
    suspend fun sendTransaction(transaction: Transaction): TransactionResult
}