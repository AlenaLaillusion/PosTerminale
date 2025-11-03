package com.example.server.data.storage

import android.util.Log
import com.example.server.domain.model.Transaction


class TransactionStorage {

    private val cache = mutableMapOf<String, Transaction>()

    /**
     * "Сохраняет" транзакцию — просто печатает и кладёт в память.
     */
    fun saveTransaction(transaction: Transaction) {
        cache[transaction.transactionId] = transaction
        Log.d("TransactionStorage",
            """
            Transaction saved:
            ID: ${transaction.transactionId}
            Merchant: ${transaction.merchantId}
            PAN: ${transaction.cardPan}
            Amount: ${transaction.amount}
            """.trimIndent()
        )
        //Timestamp: ${transaction.timestamp}
    }

    /**
     * Имитирует поиск транзакции по ID.
     */
    fun getTransaction(id: String): Transaction? {
        Log.d("TransactionStorage", "Lookup transaction by ID: $id")
        return cache[id]
    }

    /**
     * Закрывает хранилище (ничего не делает в этой реализации).
     */
    fun close() {
        Log.d("TransactionStorage","Storage closed (in-memory only).")
    }
}