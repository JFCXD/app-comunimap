package com.example.comunimap.api

import com.example.comunimap.models.SubirImagenResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// =====================================================
// SERVICIO RETROFIT - LARAVEL COMUNIMAP
// =====================================================

// Endpoint especializado para gestión de archivos multimedia en Laravel
interface LaravelApiService {

    @Multipart
    @POST("api/subir-imagen-reporte")
    suspend fun subirImagen(
        @Part imagen: MultipartBody.Part
    ): Response<SubirImagenResponse>
}
