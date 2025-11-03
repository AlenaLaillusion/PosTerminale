package com.example.posterminale.presentation.di

import com.example.posterminale.BuildConfig
import com.example.posterminale.data.ApiService
import com.example.posterminale.data.network.RetrofitClient
import com.example.posterminale.data.network.TcpClient
import com.example.posterminale.domain.Transport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt-модуль для транспортного уровня.
 * Позволяет переключаться между TCP и HTTP реализациями.
 */
@Module
@InstallIn(SingletonComponent::class)
object TransportModule {

    // --- CONFIG ---
    @Provides
    @Singleton
    @Named("useHttpTransport")
    fun provideUseHttpTransportFlag(): Boolean = BuildConfig.USE_HTTP // переключай BuildConfig.USE_HTTP_TRANSPORT

    @Provides @Singleton @Named("tcpHost")
    fun provideTcpHost(): String = BuildConfig.TCP_HOST

    @Provides @Singleton @Named("tcpPort")
    fun provideTcpPort(): Int = BuildConfig.TCP_PORT

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://192.168.1.10:8080/") // адаптируй под свой backend
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(okHttp)
            .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    // --- IMPLEMENTATIONS ---
    @Provides @Singleton
    fun provideTcpClient(
        @Named("tcpHost") host: String,
        @Named("tcpPort") port: Int
    ): TcpClient = TcpClient(host, port)

    @Provides @Singleton
    fun provideRetrofitClient(api: ApiService): RetrofitClient = RetrofitClient(api)

    // --- SELECTOR ---
    @Provides @Singleton
    fun provideTransport(
        @Named("useHttpTransport") useHttp: Boolean,
        tcp: TcpClient,
        http: RetrofitClient
    ): Transport = if (useHttp) http else tcp
}