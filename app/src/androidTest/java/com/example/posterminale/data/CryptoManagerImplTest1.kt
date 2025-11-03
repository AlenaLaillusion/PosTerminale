package com.example.posterminale.data

import com.example.posterminale.domain.CryptoManagerContract
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class CryptoManagerImplTest {

 private lateinit var crypto: CryptoManagerContract
 private lateinit var secureRandom: SecureRandom
 private lateinit var aesKey: SecretKey
 private lateinit var rsaKeyPair: KeyPair

 @Before
 fun setup() {
  secureRandom = SecureRandom()
  crypto = CryptoManagerImpl(secureRandom)

  // AES key
  val keyGen = KeyGenerator.getInstance("AES")
  keyGen.init(256)
  aesKey = keyGen.generateKey()

  // RSA keys
  val kpg = KeyPairGenerator.getInstance("RSA")
  kpg.initialize(2048)
  rsaKeyPair = kpg.generateKeyPair()
 }

 // --- AES-GCM round-trip test ---
 @Test
 fun aesGcmEncryptDecrypt_ReturnsSamePlaintext() {
  val data = "Secure message for AES".toByteArray()
  val iv = crypto.generateIv()

  val encrypted = crypto.encryptAesGcm(data, aesKey, iv)
  val decrypted = crypto.decryptAesGcm(encrypted, aesKey, iv)

  assertArrayEquals(data, decrypted)
 }

 // --- RSA-OAEP round-trip test ---
 @Test
 fun rsaOaepEncryptDecrypt_ReturnsSameData() {
  val data = "Secret session key".toByteArray()
  val encrypted = crypto.encryptRsaOaep(data, rsaKeyPair.public)
  val decrypted = crypto.decryptRsaOaep(encrypted, rsaKeyPair.private)
  assertArrayEquals(data, decrypted)
 }

 // --- HMAC generation and verification ---
 @Test
 fun hmacVerify_ReturnsTrueForValidSignature() {
  val data = "Integrity check".toByteArray()
  val keyBytes = ByteArray(32).apply { secureRandom.nextBytes(this) }

  val hmac = crypto.hmacSha256(data, keyBytes)
  val verified = crypto.verifyHmac(data, keyBytes, hmac)

  assertTrue(verified)
 }

 @Test
 fun hmacVerify_ReturnsFalseForTamperedData() {
  val data = "Integrity check".toByteArray()
  val keyBytes = ByteArray(32).apply { secureRandom.nextBytes(this) }

  val hmac = crypto.hmacSha256(data, keyBytes)
  val tampered = "Integrity fail".toByteArray()

  val verified = crypto.verifyHmac(tampered, keyBytes, hmac)
  assertFalse(verified)
 }

 // --- AES key and IV generation ---
 @Test
 fun generatedKeysAndIvs_AreUnique() {
  val key1 = crypto.generateAesKey().encoded
  val key2 = crypto.generateAesKey().encoded
  val iv1 = crypto.generateIv()
  val iv2 = crypto.generateIv()

  assertFalse(key1.contentEquals(key2))
  assertFalse(iv1.contentEquals(iv2))
 }

 // --- HMAC deterministic test ---
 @Test
 fun hmac_IsDeterministicForSameKeyAndData() {
  val data = "Consistency test".toByteArray()
  val key = ByteArray(32).apply { secureRandom.nextBytes(this) }

  val hmac1 = crypto.hmacSha256(data, key)
  val hmac2 = crypto.hmacSha256(data, key)

  assertArrayEquals(hmac1, hmac2)
 }

}