package com.example.api

import com.example.models.Categoria
import com.example.models.CrearReporteRequest
import com.example.models.CrearReporteResponse
import com.example.models.Reporte
import com.example.models.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// ====================================================================================================
// 🌐 INTERFAZ RETROFIT: SERVICIOS Y ENDPOINTS DE LA API REST REAL (PHP / MySQL `gestion_alertas`)
// ====================================================================================================
// Endpoints reales disponibles en el backend PHP:
// 1. GET `usuarios.php`      -> Lista de usuarios registrados
// 2. GET `categorias.php`    -> Lista de categorías activas
// 3. GET `reportes.php`      -> Lista de todos los reportes
// 4. POST `crear_reporte.php` -> Registrar un nuevo reporte ciudadano
// ====================================================================================================

interface ApiService {

    /**
     * 👥 Obtiene la lista de usuarios registrados en el sistema.
     * Endpoint: `usuarios.php` (GET)
     */
    @GET("usuarios.php")
    suspend fun getUsuarios(): Response<List<Usuario>>

    /**
     * 🏷️ Obtiene el catálogo de categorías disponibles.
     * Endpoint: `categorias.php` (GET)
     */
    @GET("categorias.php")
    suspend fun getCategorias(): Response<List<Categoria>>

    /**
     * 📋 Obtiene la lista completa de reportes e incidentes.
     * Endpoint: `reportes.php` (GET)
     */
    @GET("reportes.php")
    suspend fun getReportes(): Response<List<Reporte>>

    /**
     * 📤 Registra un nuevo reporte en la base de datos MySQL.
     * Endpoint: `crear_reporte.php` (POST)
     */
    @POST("crear_reporte.php")
    suspend fun crearReporte(
        @Body request: CrearReporteRequest
    ): Response<CrearReporteResponse>
}

