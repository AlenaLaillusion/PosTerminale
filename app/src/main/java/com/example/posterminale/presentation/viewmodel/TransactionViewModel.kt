package com.example.posterminale.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posterminale.domain.model.Transaction
import com.example.posterminale.domain.model.TransactionResult
import com.example.posterminale.domain.usecase.SendTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val sendTransaction: SendTransactionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<TransactionResult?>(null)
    val state = _state.asStateFlow()

    fun send(amount: Long, pan: String, merchant: String) {
        viewModelScope.launch {
            val transaction = Transaction(
                cardPan = pan,
                amount = amount,
                transactionId = UUID.randomUUID().toString(),
                merchantId = merchant
            )
            _state.value = sendTransaction(transaction)
            Timber.d( "Transaction sent: $transaction")
        }
    }
}