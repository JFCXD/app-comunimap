package com.example.comunimap.api

import com.example.comunimap.models.ActualizarEstadoReporteRequest
import com.example.comunimap.models.ActualizarReporteRequest
import com.example.comunimap.models.ActualizarUsuarioRequest
import com.example.comunimap.models.ApiResponse
import com.example.comunimap.models.CambiarEstadoUsuarioRequest
import com.example.comunimap.models.CategoriasResponse
import com.example.comunimap.models.CrearReporteRequest
import com.example.comunimap.models.CrearUsuarioRequest
import com.example.comunimap.models.LoginRequest
import com.example.comunimap.models.LoginResponse
import com.example.comunimap.models.ReportesResponse
import com.example.comunimap.models.UsuariosResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// =====================================================
// INTERFAZ DE SERVICIO RETROFIT - COMUNIMAP
// =====================================================

// Define los endpoints para interactuar con la API en Railway.
// Todos los endpoints de mutación envían objetos en formato JSON con @Body.
interface ApiService {

    // -------------------------------------------------
    // AUTENTICACIÓN
    // -------------------------------------------------

    // Inicia sesión verificando credenciales en MySQL a través de login.php
    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // -------------------------------------------------
    // LECTURA DE DATOS
    // -------------------------------------------------

    // Consulta la lista de usuarios reales almacenados en MySQL.
    @GET("usuarios.php")
    suspend fun obtenerUsuarios(): Response<UsuariosResponse>

    // Consulta las categorías reales registradas en el sistema.
    @GET("categorias.php")
    suspend fun obtenerCategorias(): Response<CategoriasResponse>

    // Consulta la lista completa de reportes urbanos registrados.
    @GET("reportes.php")
    suspend fun obtenerReportes(): Response<ReportesResponse>

    // -------------------------------------------------
    // GESTIÓN DE USUARIOS
    // -------------------------------------------------

    // Crea un nuevo usuario en la base de datos MySQL.
    @POST("crear_usuario.php")
    suspend fun crearUsuario(@Body request: CrearUsuarioRequest): Response<ApiResponse>

    // Actualiza los datos de un usuario existente.
    @POST("actualizar_usuario.php")
    suspend fun actualizarUsuario(@Body request: ActualizarUsuarioRequest): Response<ApiResponse>

    // Cambia el estado (Activo / Inactivo) de un usuario.
    @POST("cambiar_estado_usuario.php")
    suspend fun cambiarEstadoUsuario(@Body request: CambiarEstadoUsuarioRequest): Response<ApiResponse>

    // -------------------------------------------------
    // GESTIÓN DE REPORTES
    // -------------------------------------------------

    // Registra un nuevo reporte con coordenadas y categoría.
    @POST("crear_reporte.php")
    suspend fun crearReporte(@Body request: CrearReporteRequest): Response<ApiResponse>

    // Actualiza los datos de un reporte existente.
    @POST("actualizar_reporte.php")
    suspend fun actualizarReporte(@Body request: ActualizarReporteRequest): Response<ApiResponse>

    // Actualiza el estado (Pendiente / En Proceso / Solucionado) de un reporte.
    @POST("actualizar_estado.php")
    suspend fun actualizarEstadoReporte(@Body request: ActualizarEstadoReporteRequest): Response<ApiResponse>
}
