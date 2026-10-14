package com.example.comunimap.api

import com.example.comunimap.utils.BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

// =====================================================
// CLIENTE HTTP (RETROFIT + MOSHI + OKHTTP)
// =====================================================

// Objeto singleton responsable de inicializar y proveer la instancia de ApiService.
object ApiClient {

    // Interceptor para registrar en Logcat los encabezados y cuerpos de las peticiones HTTP
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configuración del cliente OkHttp con tiempos de espera de 15 segundos
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    // Constructor de Moshi para la serialización y deserialización de JSON
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Configuración de Retrofit con la URL base fija de Railway y el convertidor Moshi
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // Instancia pública para realizar las llamadas de red en la aplicación
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
