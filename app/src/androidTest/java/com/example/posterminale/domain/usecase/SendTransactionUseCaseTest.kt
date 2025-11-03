package com.example.posterminale.domain.usecase

import com.example.posterminale.domain.model.Transaction
import com.example.posterminale.domain.model.TransactionResult
import com.example.posterminale.domain.model.TransactionStatus
import com.example.posterminale.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

 class SendTransactionUseCaseTest {

  private lateinit var repository: TransactionRepository
  private lateinit var useCase: SendTransactionUseCase

  @Before
  fun setup() {
   repository = mockk()
   useCase = SendTransactionUseCase(repository)
  }

  @Test
  fun invokeRepositoryReturnsResult() = runTest {
   val transaction = Transaction(
    cardPan = "4111111111111111",
    amount = 1000L,
    transactionId = "tx123",
    merchantId = "m001"
   )
   val expectedResult = TransactionResult(
    status = TransactionStatus.APPROVED,
    authCode = "123456",
    timestamp = System.currentTimeMillis()
   )

   coEvery { repository.sendTransaction(transaction) } returns expectedResult

   // when
   val result = useCase(transaction)

   // then
   assertEquals(expectedResult, result)
   coVerify (exactly = 1) { repository.sendTransaction(transaction) }
  }

  @Test(expected = RuntimeException::class)
  fun invokeExceptionRepositoryFails() = runTest {
   val transaction = Transaction("4000000000000000", 500L, "tx999", "m777")
   coEvery { repository.sendTransaction(transaction) } throws RuntimeException("Network error")

   // when
   useCase(transaction) // should throw
  }

}