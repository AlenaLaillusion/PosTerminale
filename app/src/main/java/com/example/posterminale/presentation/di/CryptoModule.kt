package com.example.posterminale.presentation.di

import android.content.Context
import android.util.Base64
import com.example.posterminale.R
import com.example.posterminale.data.CryptoManagerImpl
import com.example.posterminale.domain.CryptoManagerContract
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideServerPublicKey(@ApplicationContext context: Context): PublicKey {
        val inputStream = context.resources.openRawResource(R.raw.public_key)
        return try {
            val keyBytes = inputStream.readBytes()
            val publicKeyPEM = String(keyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")

            val decoded = Base64.decode(publicKeyPEM, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(decoded)
            KeyFactory.getInstance("RSA").generatePublic(keySpec)
        } finally {
            inputStream.close()
        }
    }
}