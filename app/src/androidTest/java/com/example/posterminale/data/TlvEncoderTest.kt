package com.example.posterminale.data

import com.example.posterminale.domain.model.Transaction
import org.junit.Assert.*

import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class TlvEncoderTest {

 @Test
 fun encodeTransactionBuildsValidTLVStructure() {
  val tx = Transaction(
   cardPan = "4111111111111111",
   amount = 123456L,
   transactionId = "TX1001",
   merchantId = "M001"
  )

  val encoded = TlvEncoder.encodeTransaction(tx)

  // Проверим, что все теги есть
  assertTrue(encoded.contains(0x10))
  assertTrue(encoded.contains(0x20))
  assertTrue(encoded.contains(0x30))
  assertTrue(encoded.contains(0x40))

  // Проверим, что PAN закодирован корректно
  val panBytes = "4111111111111111".toByteArray(StandardCharsets.UTF_8)
  assertTrue(encoded.toList().windowed(panBytes.size) { it.toByteArray() }.any { it.contentEquals(panBytes) })

  // Проверим, что длина PAN записана в TLV
  val panLenIndex = encoded.indexOf(0x10.toByte()) + 1
  val lenBytes = encoded.sliceArray(panLenIndex until panLenIndex + 2)
  val length = ByteBuffer.wrap(lenBytes).order(ByteOrder.BIG_ENDIAN).short.toInt()
  assertEquals(panBytes.size, length)
 }

 @Test
 fun encodeAmountProducesReversedByteOrder() {
  val amount = 123456L
  val encoded = TlvEncoder.encodeAmount(amount)

  val normal = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(amount).array()
  val reversed = normal.reversedArray()

  assertArrayEquals(reversed, encoded)
 }

 @Test
 fun tLVPacketConcatenatesAllFields() {
  val tx = Transaction("4111", 10L, "T1", "M1")
  val encoded = TlvEncoder.encodeTransaction(tx)

  // должно содержать все 4 тега подряд
  val tags = listOf(0x10.toByte(), 0x20.toByte(), 0x30.toByte(), 0x40.toByte())
  for (tag in tags) assertTrue(encoded.contains(tag))
 }

}