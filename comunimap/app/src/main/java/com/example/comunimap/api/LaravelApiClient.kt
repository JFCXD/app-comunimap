package com.example.comunimap.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

// =====================================================
// CLIENTE HTTP PARA SERVICIOS LARAVEL
// =====================================================

// Cliente independiente para interactuar con la instancia Laravel en Railway
object LaravelApiClient {

    // URL base de Laravel para almacenamiento y subida de archivos
    private const val LARAVEL_BASE_URL = "https://comunimap-production.up.railway.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(LARAVEL_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: LaravelApiService = retrofit.create(LaravelApiService::class.java)

    // Alias conveniente para acceso directo
    val api: LaravelApiService get() = apiService
}
