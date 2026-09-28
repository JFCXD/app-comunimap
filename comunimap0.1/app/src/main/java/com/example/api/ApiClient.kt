package com.example.api

import com.example.models.Categoria
import com.example.models.CrearReporteRequest
import com.example.models.Reporte
import com.example.models.Usuario
import com.example.utils.Constantes
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

// ====================================================================================================
// 🌐 CLIENTE HTTP (RETROFIT + OKHTTP + MOSHI) - COMUNICACIÓN CON BASE DE DATOS REMOTA
// ====================================================================================================
// Este objeto singleton se encarga de crear y configurar la conexión de red hacia el servidor backend
// (por ejemplo un servidor Apache/XAMPP con PHP y base de datos MySQL).
//
// 📌 ¿CÓMO MODIFICAR LA DIRECCIÓN IP O PUERTO DEL SERVIDOR?
// 1. En un Emulador Oficial de Android: `http://10.0.2.2/api_gestion/` (representa el localhost de tu PC).
// 2. En un Celular Físico conectado por WiFi: `http://192.168.1.XX/api_gestion/` (tu IP local de red).
// 3. En un Servidor Cloud en Producción: `https://tuservidor.com/api/`
// ====================================================================================================

object ApiClient {

    // URL base de conexión activa
    private var currentBaseUrl: String = Constantes.DEFAULT_BASE_URL
    private var apiService: ApiService? = null

    // ------------------------------------------------------------------------------------------------
    // ⚙️ CONFIGURACIÓN DE MOSHI (Serializador JSON a Kotlin)
    // ------------------------------------------------------------------------------------------------
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // ------------------------------------------------------------------------------------------------
    // ⏱️ CONFIGURACIÓN DE OKHTTP (Tiempos de espera y Logs de depuración)
    // ------------------------------------------------------------------------------------------------
    private fun getOkHttpClient(): OkHttpClient {
        // Muestra en la consola Logcat de Android Studio el contenido de las peticiones y respuestas JSON
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS) // Tiempo máximo para establecer contacto con el servidor
            .readTimeout(8, TimeUnit.SECONDS)    // Tiempo máximo para recibir la respuesta de la base de datos
            .writeTimeout(8, TimeUnit.SECONDS)   // Tiempo máximo para enviar datos (peticiones POST/PUT)
            .addInterceptor(logging)
            .build()
    }

    /**
     * 🏭 Provee la instancia activa de Retrofit.
     * Si la URL cambia dinámicamente, reconstruye el servicio automáticamente.
     */
    @Synchronized
    fun getService(baseUrl: String = currentBaseUrl): ApiService {
        if (apiService == null || baseUrl != currentBaseUrl) {
            val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            currentBaseUrl = formattedUrl

            val retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(getOkHttpClient())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }

    fun getBaseUrl(): String = currentBaseUrl

    /**
     * 🔄 Permite actualizar la IP de la base de datos en tiempo de ejecución
     */
    fun updateBaseUrl(newUrl: String) {
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        currentBaseUrl = formatted
        apiService = null
    }
}

// ====================================================================================================
// 🗄️ REPOSITORIO DE DATOS: GESTIÓN DE ALERTAS (REPOSITORY PATTERN)
// ====================================================================================================
// Esta clase actúa como la única fuente de verdad (Single Source of Truth) para la aplicación.
// Implementa un sistema de alta disponibilidad con:
// 1. Intento de sincronización en tiempo real con MySQL mediante la API PHP.
// 2. Modo Offline / Cache en memoria para que la app nunca se quede congelada si se pierde la conexión.
// ====================================================================================================

class AlertasRepository {

    // ------------------------------------------------------------------------------------------------
    // 📦 ESTADOS REACTIVOS (StateFlow): Notifican automáticamente a la interfaz Compose cuando hay cambios
    // ------------------------------------------------------------------------------------------------
    private val _reportes = MutableStateFlow<List<Reporte>>(emptyList())
    val reportes: StateFlow<List<Reporte>> = _reportes.asStateFlow()

    private val _categorias = MutableStateFlow<List<Categoria>>(emptyList())
    val categorias: StateFlow<List<Categoria>> = _categorias.asStateFlow()

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _estaConectadoApi = MutableStateFlow<Boolean?>(null)
    val estaConectadoApi: StateFlow<Boolean?> = _estaConectadoApi.asStateFlow()

    private val _ultimoMensajeApi = MutableStateFlow<String>("")
    val ultimoMensajeApi: StateFlow<String> = _ultimoMensajeApi.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    // ------------------------------------------------------------------------------------------------
    // 🔄 MÉTODOS DE CONSULTA Y SINCRONIZACIÓN REAL
    // ------------------------------------------------------------------------------------------------

    /**
     * 📥 Descarga la información real más reciente desde el servidor PHP/MySQL.
     */
    suspend fun cargarDatosRemotos() = withContext(Dispatchers.IO) {
        _estaCargando.value = true
        try {
            val service = ApiClient.getService()

            // 1. Cargar Reportes reales desde MySQL (`reportes.php`)
            val respuestaReportes = try { service.getReportes() } catch (e: Exception) { null }
            if (respuestaReportes != null && respuestaReportes.isSuccessful) {
                _reportes.value = respuestaReportes.body() ?: emptyList()
                _estaConectadoApi.value = true
                _ultimoMensajeApi.value = "Sincronizado con base de datos MySQL"
            } else {
                _estaConectadoApi.value = false
                _ultimoMensajeApi.value = "Error al conectar con reportes.php"
            }

            // 2. Cargar Categorías reales desde MySQL (`categorias.php`)
            try {
                val respuestaCategorias = service.getCategorias()
                if (respuestaCategorias.isSuccessful) {
                    _categorias.value = respuestaCategorias.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Log o estado si falla categorias
            }

            // 3. Cargar Usuarios reales desde MySQL (`usuarios.php`)
            try {
                val respuestaUsuarios = service.getUsuarios()
                if (respuestaUsuarios.isSuccessful) {
                    _usuarios.value = respuestaUsuarios.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Log o estado si falla usuarios
            }

        } catch (e: Exception) {
            _estaConectadoApi.value = false
            _ultimoMensajeApi.value = "Error de conexión: ${e.localizedMessage ?: "Servidor inalcanzable"}"
        } finally {
            _estaCargando.value = false
        }
    }

    /**
     * 📤 Registra un nuevo reporte ciudadano en `crear_reporte.php`.
     * Si responde success = true, vuelve a consultar `reportes.php` para recargar los datos reales.
     * Si falla, muestra el mensaje de error y NO agrega datos falsos en local.
     */
    suspend fun crearReporte(request: CrearReporteRequest): Boolean = withContext(Dispatchers.IO) {
        _estaCargando.value = true
        var exito = false

        try {
            val service = ApiClient.getService()
            val respuesta = service.crearReporte(request)

            if (respuesta.isSuccessful && respuesta.body()?.success == true) {
                _ultimoMensajeApi.value = respuesta.body()?.mensaje ?: "Reporte registrado correctamente"
                exito = true

                // Volver a consultar reportes.php para traer la lista actualizada desde MySQL
                try {
                    val reportesActualizados = service.getReportes()
                    if (reportesActualizados.isSuccessful && reportesActualizados.body() != null) {
                        _reportes.value = reportesActualizados.body()!!
                    }
                } catch (ignored: Exception) {}
            } else {
                val errorMsg = respuesta.body()?.mensaje ?: "Error al registrar reporte en el servidor"
                _ultimoMensajeApi.value = errorMsg
                exito = false
            }
        } catch (e: Exception) {
            _ultimoMensajeApi.value = "Error de red al crear reporte: ${e.localizedMessage}"
            exito = false
        } finally {
            _estaCargando.value = false
        }
        exito
    }
}

