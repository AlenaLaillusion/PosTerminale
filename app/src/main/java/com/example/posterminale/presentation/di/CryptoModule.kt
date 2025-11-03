package com.example.posterminale.presentation.di

import android.util.Base64
import com.example.posterminale.data.CryptoManagerImpl
import com.example.posterminale.domain.CryptoManagerContract
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.security.KeyFactory
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class CryptoBindsModule {
    @Binds
    @Singleton
    abstract fun bindCryptoManager(impl: CryptoManagerImpl): CryptoManagerContract
}

@Module
@InstallIn(SingletonComponent::class)
object CryptoProvidesModule {

    @Provides
    @Singleton
    fun provideSecureRandom() = SecureRandom()

    @Provides
    @Singleton
    @Named("HmacKey")
    fun provideHmacKey(): ByteArray = "supersecretkey123".toByteArray()

    @Provides
    @Singleton
    @Named("ServerPublicKey")
    fun provideServerPublicKey(): PublicKey {
        // Твой публичный ключ в Base64
        val base64Key = """
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzUJIOmwRTAawOwlAx1RQ
aBKQzVxFhcX8c74F5PVuxonLp/h8w+3wZkHn+MqmVdhFvfGMrmK98MTxxReozZog
mUp0pNGF2JVcza1YJQ8Ipkml4rQ7RiWxDLebP1IPG+5PEZyTKsD7lR77o9Jkln/i
nQun7OdgOBW3wZZDkR/VEqw/zt2OZ4+GLORMro2dy+C/MEE6BQQA0j9x8D3HMjHK
eVaSXdhiBXh2xirjxMIbe6yGme2VBsStD3g63lP54b0EKN9c9KOudp742p5cu+Hd
4eklQAHk736hGDW+mkHZgxR/4nauMJMnlJnpRP2l3sZ+v1lsmRN4iAsxcE8LUIk5
LwIDAQAB
    """.trimIndent().replace("\n", "")

        val keyBytes = Base64.decode(base64Key, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }
}