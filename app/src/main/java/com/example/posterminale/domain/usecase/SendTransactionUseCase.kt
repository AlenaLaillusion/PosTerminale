package com.example.posterminale.domain.usecase

import com.example.posterminale.domain.model.Transaction
import com.example.posterminale.domain.model.TransactionResult
import com.example.posterminale.domain.repository.TransactionRepository
import javax.inject.Inject

class SendTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): TransactionResult {
        return repository.sendTransaction(transaction)
    }
}