package com.example.data.storage

import com.example.domain.model.Transaction


class TransactionStorage {

    private val cache = mutableMapOf<String, Transaction>()

    /**
     * "Сохраняет" транзакцию — просто печатает и кладёт в память.
     */
    fun saveTransaction(transaction: Transaction) {
        cache[transaction.transactionId] = transaction
        println(
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
        println( "Lookup transaction by ID: $id")
        return cache[id]
    }

    /**
     * Закрывает хранилище (ничего не делает в этой реализации).
     */
    fun close() {
        println("Storage closed (in-memory only).")
    }
}